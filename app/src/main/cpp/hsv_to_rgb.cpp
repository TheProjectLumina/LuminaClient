//

//
/*
 * © Project Lumina 2025 — Licensed under GNU GPLv3
 * You are free to use, modify, and redistribute this code under the terms
 * of the GNU General Public License v3. See the LICENSE file for details.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * This is open source — not open credit.
 *
 * If you're here to build, welcome. If you're here to repaint and reupload
 * with your tag slapped on it… you're not fooling anyone.
 *
 * Changing colors and class names doesn't make you a developer.
 * Copy-pasting isn't contribution.
 *
 * You have legal permission to fork. But ask yourself — are you improving,
 * or are you just recycling someone else's work to feed your ego?
 *
 * Open source isn't about low-effort clones or chasing clout.
 * It's about making things better. Sharper. Cleaner. Smarter.
 *
 * So go ahead, fork it — but bring something new to the table,
 * or don’t bother pretending.
 *
 * This message is philosophical. It does not override your legal rights under GPLv3.
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * GPLv3 Summary:
 * - You have the freedom to run, study, share, and modify this software.
 * - If you distribute modified versions, you must also share the source code.
 * - You must keep this license and copyright intact.
 * - You cannot apply further restrictions — the freedom stays with everyone.
 * - This license is irrevocable, and applies to all future redistributions.
 *
 * Full text: https://www.gnu.org/licenses/gpl-3.0.html
 */
#include "hsv_to_rgb.h"
#include <vector>
#include <cmath>
#include <algorithm>


static std::vector<RGB> colorTable;
static const int TABLE_SIZE = 360;

void initColorTable() {
    if (!colorTable.empty()) return;
    colorTable.resize(TABLE_SIZE);
    for (int i = 0; i < TABLE_SIZE; ++i) {
        float h = static_cast<float>(i) / TABLE_SIZE;
        float s = 1.0f;
        float v = 1.0f;
        float hDegrees = h * 360.0f;
        float c = v * s;
        float x = c * (1.0f - std::fabs(std::fmod(hDegrees / 60.0f, 2.0f) - 1.0f));
        float m = v - c;

        float r, g, b;
        if (hDegrees < 60) { r = c; g = x; b = 0; }
        else if (hDegrees < 120) { r = x; g = c; b = 0; }
        else if (hDegrees < 180) { r = 0; g = c; b = x; }
        else if (hDegrees < 240) { r = 0; g = x; b = c; }
        else if (hDegrees < 300) { r = x; g = 0; b = c; }
        else { r = c; g = 0; b = x; }

        colorTable[i] = {r + m, g + m, b + m};
    }
}

RGB hsvToRgb(float h) {

    initColorTable();


    int index = static_cast<int>(h * TABLE_SIZE) % TABLE_SIZE;
    if (index < 0) index += TABLE_SIZE;

    return colorTable[index];
}