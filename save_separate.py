from PIL import Image

def save_separate_textures():
    path_full = r'C:/Users/ecrea/.gemini/antigravity/brain/740e9642-4282-48dc-a8fd-527affef0850/uploaded_media_0_1769675018523.png'
    path_empty = r'C:/Users/ecrea/.gemini/antigravity/brain/740e9642-4282-48dc-a8fd-527affef0850/uploaded_media_1_1769675018523.png'
    
    img_full = Image.open(path_full).convert('RGBA')
    img_empty = Image.open(path_empty).convert('RGBA')
    
    # Scale to 256 width
    target_w = 256
    w, h = img_full.size
    scale = target_w / w
    target_h = int(h * scale)
    
    print(f"Scaling from {w}x{h} to {target_w}x{target_h}")
    
    img_full_scaled = img_full.resize((target_w, target_h), Image.Resampling.LANCZOS)
    img_empty_scaled = img_empty.resize((target_w, target_h), Image.Resampling.LANCZOS)
    
    # Save as separate files
    base_dir = 'src/main/resources/assets/elementmagicboss/textures/gui/boss_bars'
    img_full_scaled.save(f'{base_dir}/wizard_full.png')
    img_empty_scaled.save(f'{base_dir}/wizard_empty.png')
    
    print(f"Saved wizard_full.png and wizard_empty.png")
    
    # Print constants for Java
    print("CONSTANTS_START")
    print(f"TEX_WIDTH = 256")
    print(f"TEX_HEIGHT = {target_h}") # Since it's a single state per file
    
    display_h = int(182 * (target_h / target_w))
    print(f"DISPLAY_WIDTH = 182")
    print(f"DISPLAY_HEIGHT = {display_h}")
    print("CONSTANTS_END")

if __name__ == '__main__':
    save_separate_textures()
