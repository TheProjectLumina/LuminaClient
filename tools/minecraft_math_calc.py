import tkinter as tk
from tkinter import ttk, messagebox
import math
import random

FORMULAS = {
    "Yaw to Target (°)": {
        "inputs": ["deltaX", "deltaZ"],
        "formula": lambda deltaX, deltaZ: (math.degrees(math.atan2(deltaZ, deltaX)) - 90 + 360) % 360
    },
    "Motion Vector": {
        "inputs": ["yaw", "speed"],
        "formula": lambda yaw, speed: (
            math.cos(math.radians(yaw)) * speed,
            math.sin(math.radians(yaw)) * speed
        )
    },
    "Distance 2D": {
        "inputs": ["deltaX", "deltaZ"],
        "formula": lambda deltaX, deltaZ: math.sqrt(deltaX ** 2 + deltaZ ** 2)
    },
    "Strafe Offset": {
        "inputs": ["angle", "radius"],
        "formula": lambda angle, radius: (
            radius * math.cos(math.radians(angle)),
            radius * math.sin(math.radians(angle))
        )
    },
    "Attack Delay (ms)": {
        "inputs": ["cps", "randomize"],
        "formula": lambda cps, randomize: (1000 / cps) * (random.uniform(0.8, 1.2) if randomize else 1)
    }
}

class CheatMathGUI:
    def __init__(self, root):
        self.root = root
        self.root.title("Minecraft Cheat Math Tool")
        self.variables = {}
        
        self.formula_var = tk.StringVar()
        self.log_text = tk.StringVar()
        self.log_text.set("Select a formula and enter values.")

        ttk.Label(root, text="Select Formula:").pack(anchor="w", padx=10)
        self.formula_menu = ttk.Combobox(root, textvariable=self.formula_var, values=list(FORMULAS.keys()), state="readonly")
        self.formula_menu.pack(fill="x", padx=10, pady=5)
        self.formula_menu.bind("<<ComboboxSelected>>", self.update_inputs)

        self.input_frame = ttk.Frame(root)
        self.input_frame.pack(fill="x", padx=10)

        self.calc_btn = ttk.Button(root, text="Calculate", command=self.calculate)
        self.calc_btn.pack(pady=10)

        ttk.Label(root, text="Output:").pack(anchor="w", padx=10)
        self.output_box = tk.Text(root, height=6, wrap="word", bg="#1e1e1e", fg="#00ff00", insertbackground="white")
        self.output_box.pack(fill="both", expand=True, padx=10, pady=5)

    def update_inputs(self, event=None):
        for widget in self.input_frame.winfo_children():
            widget.destroy()
        self.variables.clear()

        formula_key = self.formula_var.get()
        if formula_key not in FORMULAS:
            return

        inputs = FORMULAS[formula_key]["inputs"]
        for input_name in inputs:
            ttk.Label(self.input_frame, text=f"{input_name}:").pack(anchor="w")
            entry = ttk.Entry(self.input_frame)
            entry.pack(fill="x", pady=2)
            self.variables[input_name] = entry

    def calculate(self):
        formula_key = self.formula_var.get()
        if formula_key not in FORMULAS:
            messagebox.showerror("Error", "Please select a formula.")
            return

        try:
            inputs = FORMULAS[formula_key]["inputs"]
            values = []
            for name in inputs:
                val = self.variables[name].get()
                if name == "randomize":
                    values.append(val.lower() == "true")
                else:
                    values.append(float(val))

            result = FORMULAS[formula_key]["formula"](*values)
            self.output_box.insert("end", f"> {formula_key} = {result}\n")
            self.output_box.see("end")
        except Exception as e:
            messagebox.showerror("Error", f"Calculation failed:\n{e}")

if __name__ == "__main__":
    root = tk.Tk()
    app = CheatMathGUI(root)
    root.geometry("500x450")
    root.mainloop()
