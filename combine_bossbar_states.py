from PIL import Image, ImageChops
import os

def combine_images():
    # Paths to the uploaded images
    path0 = r'C:/Users/ecrea/.gemini/antigravity/brain/740e9642-4282-48dc-a8fd-527affef0850/uploaded_media_0_1769673936359.png'
    path1 = r'C:/Users/ecrea/.gemini/antigravity/brain/740e9642-4282-48dc-a8fd-527affef0850/uploaded_media_1_1769673936359.png'
    
    img0 = Image.open(path0).convert('RGBA')
    img1 = Image.open(path1).convert('RGBA')
    
    # Determine which is Empty and which is Full
    # Count non-transparent purple pixels to decide
    def count_purple(img):
        count = 0
        w, h = img.size
        pixels = img.load()
        for y in range(h):
            for x in range(w):
                r, g, b, a = pixels[x, y]
                if a > 50 and b > 100 and b > g:
                    count += 1
        return count

    c0 = count_purple(img0)
    c1 = count_purple(img1)
    
    if c0 > c1:
        full_img = img0
        empty_img = img1
        print("Image 0 identified as FULL state.")
    else:
        full_img = img1
        empty_img = img0
        print("Image 1 identified as FULL state.")
        
    # Resize to standard width 256
    target_w = 256
    w, h = full_img.size
    scale = target_w / w
    target_h = int(h * scale)
    
    print(f"Original Size: {w}x{h}")
    print(f"Scaling to: {target_w}x{target_h} (Scale: {scale:.3f})")
    
    full_scaled = full_img.resize((target_w, target_h), Image.Resampling.LANCZOS)
    empty_scaled = empty_img.resize((target_w, target_h), Image.Resampling.LANCZOS)
    
    # Combine (Empty on Top, Full on Bottom)
    tex_h = target_h * 2
    new_tex = Image.new('RGBA', (256, tex_h), (0,0,0,0))
    
    # Center horizontally just in case, though we resized to 256 width exactly
    paste_x = (256 - target_w) // 2
    
    new_tex.paste(empty_scaled, (paste_x, 0))
    new_tex.paste(full_scaled, (paste_x, target_h))
    
    out_path = 'src/main/resources/assets/elementmagicboss/textures/gui/boss_bars/wizard.png'
    new_tex.save(out_path)
    print(f"Saved combined texture to {out_path}")
    
    
    # Calculate Fill Coordinates via Difference
    diff = ImageChops.difference(empty_scaled, full_scaled)
    bbox = diff.getbbox()
    
    print("CONSTANTS_START")
    print(f"STATE_HEIGHT = {target_h}")
    display_h = int(182 * (target_h / target_w))
    print(f"DISPLAY_HEIGHT = {display_h}")
    
    if bbox:
        print(f"FILL_X = {bbox[0]}")
        print(f"FILL_Y = {bbox[1]}")
        print(f"FILL_WIDTH = {bbox[2] - bbox[0]}")
        print(f"FILL_HEIGHT = {bbox[3] - bbox[1]}")
        name_y = bbox[1] + (bbox[3] - bbox[1]) // 2 - 4
        print(f"NAME_Y = {name_y}")
    else:
        print("ERROR_NO_DIFF")
    print("CONSTANTS_END")

if __name__ == '__main__':
    combine_images()
