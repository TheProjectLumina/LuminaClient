
import tkinter as tk
from tkinter import ttk
import math

def calculate_distance():
    try:
        x1, y1, z1 = float(entry_x1.get()), float(entry_y1.get()), float(entry_z1.get())
        x2, y2, z2 = float(entry_x2.get()), float(entry_y2.get()), float(entry_z2.get())
        distance = math.sqrt((x2 - x1)**2 + (y2 - y1)**2 + (z2 - z1)**2)
        output_var.set(f"Distance: {distance:.3f}")
    except ValueError:
        output_var.set("Invalid input. Use numbers.")

def calculate_angle():
    try:
        x1, z1 = float(entry_x1.get()), float(entry_z1.get())
        x2, z2 = float(entry_x2.get()), float(entry_z2.get())
        yaw = math.degrees(math.atan2(z2 - z1, x2 - x1)) - 90
        yaw = (yaw + 360) % 360
        output_var.set(f"Yaw: {yaw:.2f}°")
    except ValueError:
        output_var.set("Invalid input. Use numbers.")

def calculate_velocity():
    try:
        speed = float(entry_speed.get())
        yaw_deg = float(entry_yaw.get())
        yaw_rad = math.radians(yaw_deg)
        vx = -math.sin(yaw_rad) * speed
        vz = math.cos(yaw_rad) * speed
        output_var.set(f"Velocity X: {vx:.3f}, Z: {vz:.3f}")
    except ValueError:
        output_var.set("Invalid input.")

def calculate_fov_difference():
    try:
        player_yaw = float(entry_player_yaw.get())
        target_yaw = float(entry_target_yaw.get())
        diff = (target_yaw - player_yaw + 180) % 360 - 180
        output_var.set(f"FOV Difference: {abs(diff):.2f}°")
    except ValueError:
        output_var.set("Invalid input.")

root = tk.Tk()
root.title("Advanced Minecraft Cheat Calculator")
root.geometry("450x500")
root.resizable(False, False)

notebook = ttk.Notebook(root)
notebook.pack(fill="both", expand=True, padx=10, pady=10)

# Coordinate Tab
tab_coords = ttk.Frame(notebook)
notebook.add(tab_coords, text="Coordinate Math")

ttk.Label(tab_coords, text="Player (x, y, z):").grid(row=0, column=0, columnspan=3, pady=5)
entry_x1, entry_y1, entry_z1 = tk.Entry(tab_coords, width=10), tk.Entry(tab_coords, width=10), tk.Entry(tab_coords, width=10)
entry_x1.grid(row=1, column=0), entry_y1.grid(row=1, column=1), entry_z1.grid(row=1, column=2)

ttk.Label(tab_coords, text="Target (x, y, z):").grid(row=2, column=0, columnspan=3, pady=5)
entry_x2, entry_y2, entry_z2 = tk.Entry(tab_coords, width=10), tk.Entry(tab_coords, width=10), tk.Entry(tab_coords, width=10)
entry_x2.grid(row=3, column=0), entry_y2.grid(row=3, column=1), entry_z2.grid(row=3, column=2)

ttk.Button(tab_coords, text="Calculate Distance", command=calculate_distance).grid(row=4, column=0, columnspan=3, pady=5)
ttk.Button(tab_coords, text="Calculate Angle (Yaw)", command=calculate_angle).grid(row=5, column=0, columnspan=3, pady=5)

# Velocity Tab
tab_velocity = ttk.Frame(notebook)
notebook.add(tab_velocity, text="Velocity Vector")

ttk.Label(tab_velocity, text="Speed:").pack(pady=5)
entry_speed = tk.Entry(tab_velocity)
entry_speed.pack()

ttk.Label(tab_velocity, text="Yaw (degrees):").pack(pady=5)
entry_yaw = tk.Entry(tab_velocity)
entry_yaw.pack()

ttk.Button(tab_velocity, text="Calculate Velocity", command=calculate_velocity).pack(pady=10)

# FOV Tab
tab_fov = ttk.Frame(notebook)
notebook.add(tab_fov, text="FOV Difference")

ttk.Label(tab_fov, text="Player Yaw:").pack(pady=5)
entry_player_yaw = tk.Entry(tab_fov)
entry_player_yaw.pack()

ttk.Label(tab_fov, text="Target Yaw:").pack(pady=5)
entry_target_yaw = tk.Entry(tab_fov)
entry_target_yaw.pack()

ttk.Button(tab_fov, text="Calculate FOV Difference", command=calculate_fov_difference).pack(pady=10)

# Output area
output_var = tk.StringVar()
ttk.Label(root, textvariable=output_var, font=("Courier", 12), foreground="green").pack(pady=10)

root.mainloop()
