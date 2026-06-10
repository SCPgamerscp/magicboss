from PIL import Image, ImageChops

def process_dual():
    # Use the absolute path to the uploaded artifact
    src_path = r'C:/Users/ecrea/.gemini/antigravity/brain/740e9642-4282-48dc-a8fd-527affef0850/uploaded_media_1769661447075.png'
    try:
        img = Image.open(src_path).convert('RGBA')
    except Exception as e:
        print(f"Failed to open image at {src_path}: {e}")
        # Fallback to local copy if moving failed (though passing abs path is safer)
        return

    w, h = img.size
    print(f"Original Size: {w}x{h}")
    
    # Assume exact vertical split
    h_s = h // 2
    
    top = img.crop((0, 0, w, h_s)) # Empty
    bot = img.crop((0, h_s, w, h)) # Full
    
    # Resize to 256 width
    target_w = 256
    scale = target_w / w
    target_h = int(h_s * scale)
    
    print(f"Scaling to: {target_w}x{target_h} (Scale: {scale:.3f})")
    
    top_scaled = top.resize((target_w, target_h), Image.Resampling.LANCZOS)
    bot_scaled = bot.resize((target_w, target_h), Image.Resampling.LANCZOS)
    
    # Create new texture
    new_tex = Image.new('RGBA', (256, target_h * 2), (0,0,0,0))
    new_tex.paste(top_scaled, (0, 0))
    new_tex.paste(bot_scaled, (0, target_h))
    
    # DEBUG: Check center of bot_scaled
    cx = target_w // 2
    cy = target_h // 2
    pix = bot_scaled.getpixel((cx, cy))
    print(f"DEBUG: bot_scaled center pixel: {pix}")
    
    # DEBUG: Check new_tex at expected fill location
    sample_y = target_h + cy
    tex_pix = new_tex.getpixel((cx, sample_y))
    print(f"DEBUG: new_tex fill area pixel: {tex_pix}")
    
    # Save
    out_path = 'src/main/resources/assets/elementmagicboss/textures/gui/boss_bars/wizard.png'
    new_tex.save(out_path)
    
    # Calculate Fill
    # Difference between Top (Empty) and Bot (Full)
    diff = ImageChops.difference(top_scaled, bot_scaled)
    bbox = diff.getbbox()
    
    print("\n--- CONSTANTS ---")
    print(f"TEX_WIDTH = 256")
    print(f"STATE_HEIGHT = {target_h}")
    
    display_w = 182
    # Maintain aspect ratio for display height
    display_h = int(182 * (target_h / target_w))
    print(f"DISPLAY_WIDTH = {display_w}")
    print(f"DISPLAY_HEIGHT = {display_h}")
    
    if bbox:
        print(f"FILL_X = {bbox[0]}")
        print(f"FILL_Y = {bbox[1]}")
        print(f"FILL_WIDTH = {bbox[2] - bbox[0]}")
        print(f"FILL_HEIGHT = {bbox[3] - bbox[1]}")
        
        # Name offset: Center of fill
        name_y = bbox[1] + (bbox[3] - bbox[1]) // 2 - 4
        print(f"NAME_Y_OFFSET = {name_y}")
        
        # Check actual fill color center
        cx = bbox[0] + (bbox[2] - bbox[0]) // 2
        cy = bbox[1] + (bbox[3] - bbox[1]) // 2
        # Offset cy by target_h to sample from bot_scaled in the texture? No, sample bot_scaled directly
        pixel = bot_scaled.getpixel((cx, cy))
        print(f"Center Pixel at {cx},{cy}: {pixel}")
    else:
        print("NO DIFFERENCE DETECTED! Images might be identical?")

if __name__ == '__main__':
    process_dual()
