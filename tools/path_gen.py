import os
import json

ASSETS_DIR = "assets/png_files"        # root folder to scan
OUTPUT_FILE = "textures.json"          # output file created


def path_to_web(path):
    """Convert a filesystem path to your desired web-style output path."""
    parts = path.split(os.sep)
    if "png_files" in parts:
        idx = parts.index("png_files")
        return "/" + "/".join(parts[idx:])
    return "/" + "/".join(parts)


def generate_json():
    result = {}

    for mob in os.listdir(ASSETS_DIR):
        mob_path = os.path.join(ASSETS_DIR, mob)
        if not os.path.isdir(mob_path):
            continue

        mob_entry = {"types": []}
        type_map = {}

        for size_dir in os.listdir(mob_path):
            size_path = os.path.join(mob_path, size_dir)
            if not os.path.isdir(size_path):
                continue

            # Extract size number "8" from "8x8"
            if "x" in size_dir:
                size_number = size_dir.split("x")[0]
            else:
                size_number = ''.join([c for c in size_dir if c.isdigit()])

            for root, _, files in os.walk(size_path):
                for file in files:
                    if not file.lower().endswith(".png"):
                        continue

                    file_path = os.path.join(root, file)
                    web_path = path_to_web(file_path)

                    variant = os.path.splitext(file)[0]

                    # Handle nested directories (e.g., state/angry.png)
                    relative_path = root.replace(size_path, "").strip(os.sep)
                    if relative_path:
                        variant = relative_path.split(os.sep)[-1]

                    # Create the variant entry if needed
                    if variant not in type_map:
                        type_map[variant] = {"textures": {}}

                    type_map[variant]["textures"][size_number] = web_path

        # Convert to ordered list with first type default
        type_list = []
        for idx, (variant, data) in enumerate(type_map.items()):
            entry = data.copy()

            if variant not in ["default", mob]:
                entry["variant"] = variant

            if idx == 0:
                entry["default"] = True

            type_list.append(entry)

        mob_entry["types"] = type_list
        result[mob] = mob_entry

    return result


if __name__ == "__main__":
    output = generate_json()

    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        json.dump(output, f, indent=2)

    print(f"✔ JSON file created: {OUTPUT_FILE}")
