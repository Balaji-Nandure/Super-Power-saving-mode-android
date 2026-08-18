import time
import psutil

class WindowsBatteryTelemetry:
    """
    Real-time battery telemetry reader for Windows Laptops.
    Reads battery level, charging state, runtime estimate, and CPU load.
    """

    def __init__(self):
        self.last_check_time = time.time()
        self.last_percent = None

    def get_telemetry(self) -> dict:
        battery = psutil.sensors_battery()
        cpu_pct = psutil.cpu_percent(interval=None)
        cpu_freq = psutil.cpu_freq()

        if not battery:
            return {
                'percent': 100,
                'power_plugged': True,
                'time_left_str': 'Plugged In',
                'secs_left': -1,
                'cpu_pct': cpu_pct,
                'cpu_freq_ghz': round(cpu_freq.current / 1000.0, 2) if cpu_freq else 0.0,
                'estimated_watts': 0.0,
                'status_label': 'Running on AC Power'
            }

        percent = int(battery.percent)
        plugged = battery.power_plugged
        secs_left = battery.secsleft

        if plugged:
            time_left_str = "Charging"
            status_label = "⚡ Charging"
            est_watts = 25.0
        else:
            if secs_left > 0 and secs_left != psutil.POWER_TIME_UNLIMITED:
                hours = int(secs_left // 3600)
                mins = int((secs_left % 3600) // 60)
                time_left_str = f"{hours}h {mins}m remaining"
            else:
                # Fallback estimation: assume standard 50Wh laptop battery
                hours = round((percent / 100.0) * 4.5, 1)
                time_left_str = f"~{hours}h estimated"

            # Estimate discharge wattage based on CPU frequency and load
            base_draw = 4.0 # Base display + motherboard
            cpu_draw = (cpu_pct / 100.0) * 15.0 # Scaled CPU load
            est_watts = round(base_draw + cpu_draw, 1)
            status_label = f"🔋 On Battery (~{est_watts}W Draw)"

        return {
            'percent': percent,
            'power_plugged': plugged,
            'time_left_str': time_left_str,
            'secs_left': secs_left,
            'cpu_pct': cpu_pct,
            'cpu_freq_ghz': round(cpu_freq.current / 1000.0, 2) if cpu_freq else 0.0,
            'estimated_watts': est_watts,
            'status_label': status_label
        }
