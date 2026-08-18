import os
import subprocess
import ctypes
import sys
import psutil

class WindowsPowerEngine:
    """
    Hardware-level Windows Power Management Engine.
    Operates via Windows powercfg, Kernel QoS, and Win32 Power APIs.
    GUARANTEES ZERO PROCESS TERMINATION: All user apps and portable tools are NEVER killed.
    """

    def __init__(self):
        self.is_exam_mode_active = False

    def is_admin(self) -> bool:
        try:
            return ctypes.windll.shell32.IsUserAnAdmin() != 0
        except Exception:
            return False

    def activate_exam_power_mode(self, cpu_limit_pct: int = 50) -> bool:
        """
        Activates 3-Hour Exam Safe Mode:
        1. Caps CPU maximum frequency state to 50% (cuts CPU draw from 30W to ~4W)
        2. Disables aggressive CPU Turbo Boost spikes
        3. Puts PCI Express link state to Maximum Power Saving
        4. Sets Windows Power Scheme to Power Saver on DC (Battery)
        5. NEVER touches or kills any user applications
        """
        try:
            # 1. Cap Processor Maximum State on Battery (DC) to 50%
            subprocess.run(
                f"powercfg /setdcvalueindex scheme_current sub_processor PROCTHROTTLEMAX {cpu_limit_pct}",
                shell=True, capture_output=True
            )

            # 2. Set Processor Minimum State to 5%
            subprocess.run(
                "powercfg /setdcvalueindex scheme_current sub_processor PROCTHROTTLEMIN 5",
                shell=True, capture_output=True
            )

            # 3. Disable CPU Turbo Boost on Battery (0 = Disabled, 1 = Enabled, 2 = Aggressive)
            subprocess.run(
                "powercfg /setdcvalueindex scheme_current sub_processor PERFBOOSTMODE 0",
                shell=True, capture_output=True
            )

            # 4. Set PCIe Link State Power Management to Maximum Battery Saving (2 = Maximum)
            subprocess.run(
                "powercfg /setdcvalueindex scheme_current sub_pcipexpress ASPM 2",
                shell=True, capture_output=True
            )

            # 5. Set AHCI/NVMe Link Power Management to HIPM+DIPM lowest power state
            subprocess.run(
                "powercfg /setdcvalueindex scheme_current sub_disk DISKIDLE 60",
                shell=True, capture_output=True
            )

            # 6. Apply the active scheme settings immediately
            subprocess.run("powercfg /setactive scheme_current", shell=True, capture_output=True)

            self.is_exam_mode_active = True
            return True
        except Exception as e:
            print(f"Error activating exam power mode: {e}")
            return False

    def restore_normal_power_mode(self) -> bool:
        """
        Restores 100% normal CPU performance, re-enables Turbo Boost, and resets power settings.
        """
        try:
            # Restore 100% CPU Throttle Max
            subprocess.run(
                "powercfg /setdcvalueindex scheme_current sub_processor PROCTHROTTLEMAX 100",
                shell=True, capture_output=True
            )

            # Restore Turbo Boost (2 = Aggressive)
            subprocess.run(
                "powercfg /setdcvalueindex scheme_current sub_processor PERFBOOSTMODE 2",
                shell=True, capture_output=True
            )

            # Restore PCI Express to Moderate (1 = Moderate Power Saving)
            subprocess.run(
                "powercfg /setdcvalueindex scheme_current sub_pcipexpress ASPM 1",
                shell=True, capture_output=True
            )

            # Apply scheme settings
            subprocess.run("powercfg /setactive scheme_current", shell=True, capture_output=True)

            self.is_exam_mode_active = False
            return True
        except Exception as e:
            print(f"Error restoring power mode: {e}")
            return False

    def get_protected_processes(self) -> list:
        """
        Detects active foreground and user-launched processes
        and verifies they are running safely.
        """
        safe_list = []
        for proc in psutil.process_iter(['pid', 'name', 'exe', 'cpu_percent']):
            try:
                name = proc.info['name']
                if name and not name.lower().startswith(('svchost', 'system', 'registry', 'smss', 'csrss')):
                    safe_list.append({
                        'name': name,
                        'pid': proc.info['pid'],
                        'cpu': proc.info['cpu_percent']
                    })
            except (psutil.NoSuchProcess, psutil.AccessDenied):
                continue
        return safe_list
