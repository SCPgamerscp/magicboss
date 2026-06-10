from PIL import Image

def get_content_bbox(img):
    pixels = img.load()
    w_p, h_p = img.size
    min_x, min_y, max_x, max_y = w_p, h_p, 0, 0
    found = False
    for y in range(h_p):
        for x in range(w_p):
            r, g, b, a = pixels[x, y]
            if a > 10 and (r > 10 or g > 10 or b > 10):
                min_x = min(min_x, x); min_y = min(min_y, y)
                max_x = max(max_x, x); max_y = max(max_y, y)
                found = True
    return (min_x, min_y, max_x+1, max_y+1) if found else (0,0,w_p,h_p)

def reprocess():
    original = Image.open('src/main/resources/assets/elementmagicboss/textures/gui/boss_bars/original_image.png').convert('RGBA')
    w, h = original.size
    h_s = h // 2
    
    top_raw = original.crop((0, 0, w, h_s))
    bot_raw = original.crop((0, h_s, w, h))
    
    bbox_top = get_content_bbox(top_raw)
    bbox_bot = get_content_bbox(bot_raw)
    
    u_x1 = min(bbox_top[0], bbox_bot[0])
    u_y1 = min(bbox_top[1], bbox_bot[1])
    u_x2 = max(bbox_top[2], bbox_bot[2])
    u_y2 = max(bbox_top[3], bbox_bot[3])
    
    union_bbox = (u_x1, u_y1, u_x2, u_y2)
    f_w = u_x2 - u_x1
    f_h = u_y2 - u_y1
    
    top_content = top_raw.crop(union_bbox)
    bot_content = bot_raw.crop(union_bbox)
    
    target_f_w = 240
    scale = target_f_w / f_w
    target_f_h = int(f_h * scale)
    
    top_scaled = top_content.resize((target_f_w, target_f_h), Image.Resampling.LANCZOS)
    bot_scaled = bot_content.resize((target_f_w, target_f_h), Image.Resampling.LANCZOS)
    
    state_h = target_f_h + 8 
    new_tex = Image.new('RGBA', (256, state_h * 2), (0, 0, 0, 0))
    paste_x = (256 - target_f_w) // 2
    paste_y = (state_h - target_f_h) // 2
    
    new_tex.paste(top_scaled, (paste_x, paste_y), top_scaled)
    new_tex.paste(bot_scaled, (paste_x, state_h + paste_y), bot_scaled)
    
    px_bot = bot_scaled.load()
    
    # SCANLINE METHOD
    # Find the y-range with a lot of purple
    y_counts = []
    for y in range(target_f_h):
        count = 0
        for x in range(target_f_w):
            r, g, b, a = px_bot[x, y]
            if a > 100 and b > 100 and b > g: # Purple-ish
                count += 1
        y_counts.append(count)
    
    # Find the max count and its y
    max_c = max(y_counts)
    if max_c > 0:
        # Determine the y range where count is at least 50% of max
        best_ys = [y for y, c in enumerate(y_counts) if c >= max_c * 0.7]
        fill_y1, fill_y2 = min(best_ys), max(best_ys)
        
        # Find x bounds on the middle y line of the detected range
        mid_y = (fill_y1 + fill_y2) // 2
        best_xs = [x for x in range(target_f_w) if px_bot[x, mid_y][3] > 100 and px_bot[x, mid_y][2] > 100]
        fill_x1, fill_x2 = min(best_xs), max(best_xs)
    else:
        # Fallback to defaults if detection fails
        fill_x1, fill_y1, fill_x2, fill_y2 = 16, 20, 224, 40
        
    new_tex.save('src/main/resources/assets/elementmagicboss/textures/gui/boss_bars/wizard.png')
    
    fill_w = fill_x2 - fill_x1 + 1
    fill_h = fill_y2 - fill_y1 + 1
    abs_fill_x = paste_x + fill_x1
    abs_fill_y = paste_y + fill_y1
    
    # We want the name centered in the region BETWEEN the top border and the bar
    # Or just centered in the frame.
    # The name in HUD is usually slightly above the center of a large bar.
    # Looking at the screenshot, y_offset 30-35 seems correct for a 64px display.
    name_y_offset = abs_fill_y + fill_h // 2 - 4
    
    print("\n--- RESULTS ---")
    print(f"STATE_HEIGHT: {state_h}")
    print(f"DISPLAY_WIDTH: 182")
    display_h = int(182 * (state_h / 256))
    print(f"DISPLAY_HEIGHT: {display_h}")
    print(f"FILL_X: {abs_fill_x}")
    print(f"FILL_Y: {abs_fill_y}")
    print(f"FILL_WIDTH: {fill_w}")
    print(f"FILL_HEIGHT: {fill_h}")
    print(f"NAME_Y_OFFSET: {name_y_offset}")

if __name__ == '__main__':
    reprocess()
