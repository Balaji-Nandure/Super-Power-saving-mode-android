import tkinter as tk
from tkinter import ttk, messagebox
import time
from power_engine import WindowsPowerEngine
from telemetry import WindowsBatteryTelemetry

class SuperPowerSaverApp:
    def __init__(self, root):
        self.root = root
        self.root.title("⚡ Super Power Saver - Windows Exam Edition")
        self.root.geometry("520x620")
        self.root.resizable(False, False)
        self.root.configure(bg="#0a0a0a")

        self.power_engine = WindowsPowerEngine()
        self.telemetry = WindowsBatteryTelemetry()

        self.setup_ui()
        self.update_telemetry_loop()

    def setup_ui(self):
        # 1. Header Banner
        header_frame = tk.Frame(self.root, bg="#0a0a0a")
        header_frame.pack(fill="x", padx=20, pady=(16, 8))

        title_label = tk.Label(
            header_frame,
            text="⚡ Super Power Saver",
            font=("Segoe UI", 18, "bold"),
            fg="#00E676",
            bg="#0a0a0a"
        )
        title_label.pack(anchor="w")

        sub_label = tk.Label(
            header_frame,
            text="3+ Hour Exam Safe Mode • Zero App Termination",
            font=("Segoe UI", 10),
            fg="#888888",
            bg="#0a0a0a"
        )
        sub_label.pack(anchor="w")

        # 2. Battery & Live Discharge HUD Card
        self.hud_card = tk.Frame(self.root, bg="#141414", highlightbackground="#222222", highlightthickness=1, padx=16, pady=14)
        self.hud_card.pack(fill="x", padx=20, pady=8)

        hud_top = tk.Frame(self.hud_card, bg="#141414")
        hud_top.pack(fill="x")

        self.tv_battery_percent = tk.Label(
            hud_top,
            text="--%",
            font=("Segoe UI", 28, "bold"),
            fg="#FFFFFF",
            bg="#141414"
        )
        self.tv_battery_percent.pack(side="left")

        self.tv_status_badge = tk.Label(
            hud_top,
            text="On Battery",
            font=("Segoe UI", 10, "bold"),
            fg="#00E676",
            bg="#1f2d24",
            padx=8,
            pady=3
        )
        self.tv_status_badge.pack(side="right")

        # Progress Bar
        self.progress_style = ttk.Style()
        self.progress_style.theme_use('default')
        self.progress_style.configure(
            "Eco.Horizontal.TProgressbar",
            thickness=8,
            troughcolor="#222222",
            background="#00E676",
            bordercolor="#141414"
        )
        self.battery_bar = ttk.Progressbar(
            self.hud_card,
            style="Eco.Horizontal.TProgressbar",
            orient="horizontal",
            length=440,
            mode="determinate"
        )
        self.battery_bar.pack(fill="x", pady=(8, 12))

        # Metrics Grid (3 Boxes: Runtime | Power | CPU Clocks)
        metrics_frame = tk.Frame(self.hud_card, bg="#141414")
        metrics_frame.pack(fill="x")

        # Box 1: Runtime
        b1 = tk.Frame(metrics_frame, bg="#1c1c1c", padx=10, pady=8)
        b1.pack(side="left", fill="both", expand=True, padx=(0, 4))
        self.tv_runtime_val = tk.Label(b1, text="--", font=("Segoe UI", 12, "bold"), fg="#00E676", bg="#1c1c1c")
        self.tv_runtime_val.pack()
        tk.Label(b1, text="EST. RUNTIME", font=("Segoe UI", 8), fg="#777777", bg="#1c1c1c").pack()

        # Box 2: Power Draw
        b2 = tk.Frame(metrics_frame, bg="#1c1c1c", padx=10, pady=8)
        b2.pack(side="left", fill="both", expand=True, padx=4)
        self.tv_power_val = tk.Label(b2, text="-- W", font=("Segoe UI", 12, "bold"), fg="#FFFFFF", bg="#1c1c1c")
        self.tv_power_val.pack()
        tk.Label(b2, text="EST. DRAW", font=("Segoe UI", 8), fg="#777777", bg="#1c1c1c").pack()

        # Box 3: CPU Clock
        b3 = tk.Frame(metrics_frame, bg="#1c1c1c", padx=10, pady=8)
        b3.pack(side="left", fill="both", expand=True, padx=(4, 0))
        self.tv_cpu_val = tk.Label(b3, text="-- GHz", font=("Segoe UI", 12, "bold"), fg="#FFFFFF", bg="#1c1c1c")
        self.tv_cpu_val.pack()
        tk.Label(b3, text="CPU CLOCK", font=("Segoe UI", 8), fg="#777777", bg="#1c1c1c").pack()

        # 3. Main Action Button: 3-Hour Exam Safe Mode Toggle
        action_frame = tk.Frame(self.root, bg="#0a0a0a")
        action_frame.pack(fill="x", padx=20, pady=10)

        self.btn_toggle_mode = tk.Button(
            action_frame,
            text="🎓 Activate 3-Hour Exam Mode",
            font=("Segoe UI", 13, "bold"),
            bg="#00E676",
            fg="#000000",
            activebackground="#00B359",
            activeforeground="#000000",
            relief="flat",
            cursor="hand2",
            padx=12,
            pady=12,
            command=self.toggle_exam_mode
        )
        self.btn_toggle_mode.pack(fill="x")

        # 4. Portable App Protection Card
        shield_card = tk.Frame(self.root, bg="#141414", highlightbackground="#222222", highlightthickness=1, padx=14, pady=12)
        shield_card.pack(fill="x", padx=20, pady=8)

        shield_title = tk.Label(
            shield_card,
            text="🛡️ Zero-Kill App Protection",
            font=("Segoe UI", 11, "bold"),
            fg="#FFFFFF",
            bg="#141414"
        )
        shield_title.pack(anchor="w")

        shield_desc = tk.Label(
            shield_card,
            text="• All user applications, portable utilities, & exam clients are NEVER killed.\n• CPU Turbo spikes (28W ➔ 4W) are clamped safely at hardware level.\n• All exam questions, proctoring cameras, & connections run 100% normal.",
            font=("Segoe UI", 9),
            fg="#999999",
            bg="#141414",
            justify="left"
        )
        shield_desc.pack(anchor="w", pady=(4, 0))

        # 5. Status Footer
        self.tv_footer = tk.Label(
            self.root,
            text="Power Mode: Normal (Full Performance)",
            font=("Segoe UI", 9),
            fg="#666666",
            bg="#0a0a0a"
        )
        self.tv_footer.pack(side="bottom", pady=12)

    def toggle_exam_mode(self):
        if not self.power_engine.is_exam_mode_active:
            success = self.power_engine.activate_exam_power_mode(cpu_limit_pct=50)
            if success:
                self.btn_toggle_mode.config(
                    text="🔴 Exam Mode Active (Click to Restore)",
                    bg="#FF3B30",
                    fg="#FFFFFF",
                    activebackground="#D32F2F"
                )
                self.tv_footer.config(
                    text="⚡ Exam Mode: ACTIVE (CPU Capped to 50% • Zero Turbo Drain • ~4W Draw)",
                    fg="#00E676"
                )
                messagebox.showinfo(
                    "Exam Mode Activated",
                    "🎓 3-Hour Exam Mode is now ACTIVE!\n\n"
                    "• CPU is clamped to ultra-low power state (~4W)\n"
                    "• Your portable apps and exam tools will continue running uninterrupted\n"
                    "• Fans will stay silent and battery runtime is maximized!"
                )
        else:
            self.power_engine.restore_normal_power_mode()
            self.btn_toggle_mode.config(
                text="🎓 Activate 3-Hour Exam Mode",
                bg="#00E676",
                fg="#000000",
                activebackground="#00B359"
            )
            self.tv_footer.config(
                text="Power Mode: Normal (Full Performance Restored)",
                fg="#666666"
            )
            messagebox.showinfo("Normal Mode Restored", "Full performance and Turbo Boost restored.")

    def update_telemetry_loop(self):
        t = self.telemetry.get_telemetry()

        self.tv_battery_percent.config(text=f"{t['percent']}%")
        self.battery_bar['value'] = t['percent']
        self.tv_status_badge.config(text=t['status_label'])
        self.tv_runtime_val.config(text=t['time_left_str'])
        self.tv_power_val.config(text=f"~{t['estimated_watts']} W")
        self.tv_cpu_val.config(text=f"{t['cpu_freq_ghz']} GHz")

        if t['percent'] <= 20 and not t['power_plugged']:
            self.progress_style.configure("Eco.Horizontal.TProgressbar", background="#FF3B30")
        else:
            self.progress_style.configure("Eco.Horizontal.TProgressbar", background="#00E676")

        self.root.after(1500, self.update_telemetry_loop)

if __name__ == "__main__":
    root = tk.Tk()
    app = SuperPowerSaverApp(root)
    root.mainloop()
