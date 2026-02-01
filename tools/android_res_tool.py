import os
import re
import tkinter as tk
from tkinter import filedialog, ttk, scrolledtext

# ---------- Utility Functions ----------
def log(message):
    output_text.config(state=tk.NORMAL)
    output_text.insert(tk.END, message + "\n")
    output_text.see(tk.END)
    output_text.config(state=tk.DISABLED)

def format_filename_for_android_res(name: str) -> str:
    base, ext = os.path.splitext(name)
    base = base.lower().replace(' ', '_').replace('-', '_')
    base = re.sub(r'[^a-z0-9_]', '', base)
    return base + ext

def rename_files_in_folder(folder_path: str, filter_exts=None):
    renamed_count = 0
    skipped_count = 0
    for filename in os.listdir(folder_path):
        if filter_exts and not filename.lower().endswith(filter_exts):
            continue
        old_path = os.path.join(folder_path, filename)
        if os.path.isfile(old_path):
            new_filename = format_filename_for_android_res(filename)
            new_path = os.path.join(folder_path, new_filename)
            if old_path != new_path:
                os.rename(old_path, new_path)
                log(f"[Renamed] {filename} → {new_filename}")
                renamed_count += 1
            else:
                log(f"[Skipped] {filename}")
                skipped_count += 1
    log(f"Done. Renamed: {renamed_count}, Skipped: {skipped_count}")

def browse_folder(entry_widget):
    folder = filedialog.askdirectory()
    if folder:
        entry_widget.delete(0, tk.END)
        entry_widget.insert(0, folder)

# ---------- GUI Setup ----------
root = tk.Tk()
root.title("Android Resource Cleaner")
root.geometry("720x500")

tabs = ttk.Notebook(root)
tabs.pack(fill=tk.BOTH, expand=True)

# ---------- Rename Fonts Tab ----------
font_tab = ttk.Frame(tabs)
tabs.add(font_tab, text="Rename Fonts")

font_frame = tk.Frame(font_tab, pady=10)
font_frame.pack(fill=tk.X, padx=10)
font_entry = tk.Entry(font_frame, width=50)
font_entry.pack(side=tk.LEFT, expand=True, fill=tk.X, padx=(0, 5))
tk.Button(font_frame, text="Browse", command=lambda: browse_folder(font_entry)).pack(side=tk.RIGHT)

tk.Button(font_tab, text="Start Renaming Fonts", command=lambda: rename_files_in_folder(font_entry.get(), ('.ttf', '.otf'))).pack(pady=5)

# ---------- Clean All File Names Tab ----------
clean_tab = ttk.Frame(tabs)
tabs.add(clean_tab, text="Clean File Names")

clean_frame = tk.Frame(clean_tab, pady=10)
clean_frame.pack(fill=tk.X, padx=10)
clean_entry = tk.Entry(clean_frame, width=50)
clean_entry.pack(side=tk.LEFT, expand=True, fill=tk.X, padx=(0, 5))
tk.Button(clean_frame, text="Browse", command=lambda: browse_folder(clean_entry)).pack(side=tk.RIGHT)

tk.Button(clean_tab, text="Start Cleaning File Names", command=lambda: rename_files_in_folder(clean_entry.get())).pack(pady=5)

# ---------- Logs Tab ----------
log_tab = ttk.Frame(tabs)
tabs.add(log_tab, text="Logs")
output_text = scrolledtext.ScrolledText(log_tab, height=20, state=tk.DISABLED, bg="black", fg="lime", font=("Courier", 10))
output_text.pack(fill=tk.BOTH, expand=True, padx=10, pady=10)

root.mainloop()
