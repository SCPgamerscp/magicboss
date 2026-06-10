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

def reprocess_single_image():
    # Load the SINGLE full illustration
    original = Image.open('src/main/resources/assets/elementmagicboss/textures/gui/boss_bars/original_image.png').convert('RGBA')
    
    # Trim to content
    bbox = get_content_bbox(original)
    original = original.crop(bbox)
    w, h = original.size
    print(f"Content Size: {w}x{h}")
    
    # Resize to target width (keeping aspect ratio)
    target_w = 250 # Leave a bit of padding in 256 texture
    scale = target_w / w
    target_h = int(h * scale)
    
    full_state = original.resize((target_w, target_h), Image.Resampling.LANCZOS)
    
    # Create Empty State by removing purple fill
    empty_state = full_state.copy()
    px = empty_state.load()
    
    fill_min_x, fill_min_y = target_w, target_h
    fill_max_x, fill_max_y = 0, 0
    
    # Define a safe zone for the bar (ignore top/bottom deco)
    safe_y_min = int(target_h * 0.25)
    safe_y_max = int(target_h * 0.85) # Avoid the very bottom ornamentation
    safe_x_min = int(target_w * 0.05)
    safe_x_max = int(target_w * 0.95)
    
    for y in range(target_h):
        for x in range(target_w):
            r, g, b, a = px[x, y]
            if a > 0:
                # Purple detection (High Blue/Red, Lower Green)
                # And strict Bounds check to avoid erasing gems on the frame border
                if y >= safe_y_min and y <= safe_y_max and x >= safe_x_min and x <= safe_x_max:
                    if b > 80 and r > 60 and g < 150 and b > g:
                        # Make it transparent (or dark gray/black if you prefer a background)
                        # Let's make it transparent so we see the world? Or semi-transparent black?
                        # Usually empty bars have a dark background.
                        # Let's make it very faint black (0,0,0,50)
                        px[x, y] = (0, 0, 0, 50)
                        
                        fill_min_x = min(fill_min_x, x)
                        fill_min_y = min(fill_min_y, y)
                        fill_max_x = max(fill_max_x, x)
                        fill_max_y = max(fill_max_y, y)

    # Stitch into texture
    state_h = target_h + 4
    tex_h = state_h * 2
    new_tex = Image.new('RGBA', (256, tex_h), (0, 0, 0, 0))
    
    paste_x = (256 - target_w) // 2
    paste_y = (state_h - target_h) // 2
    
    # Paste Empty at Top
    new_tex.paste(empty_state, (paste_x, paste_y))
    # Paste Full at Bottom
    new_tex.paste(full_state, (paste_x, state_h + paste_y))
    
    new_tex.save('src/main/resources/assets/elementmagicboss/textures/gui/boss_bars/wizard.png')
    
    # Calculate Constants
    fill_w = fill_max_x - fill_min_x + 1
    fill_h = fill_max_y - fill_min_y + 1
    
    abs_fill_x = paste_x + fill_min_x
    abs_fill_y = paste_y + fill_min_y # relative to top of the state
    
    print("\n--- RESULTS ---")
    print(f"STATE_HEIGHT: {state_h}")
    
    display_w = 182
    # ratio = display_w / target_w
    # display_h = int(target_h * ratio)
    display_h = int(182 * (target_h / target_w))
    
    print(f"DISPLAY_WIDTH: {display_w}")
    print(f"DISPLAY_HEIGHT: {display_h}")
    print(f"FILL_X: {abs_fill_x}")
    print(f"FILL_Y: {abs_fill_y}")
    print(f"FILL_WIDTH: {fill_w}")
    print(f"FILL_HEIGHT: {fill_h}")
    
    name_y = abs_fill_y + fill_h // 2 - 4
    print(f"NAME_Y_OFFSET: {name_y}")

if __name__ == '__main__':
    reprocess_single_image()
