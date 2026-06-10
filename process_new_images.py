from PIL import Image

def inspect_and_save():
    paths = [
        r'C:/Users/ecrea/.gemini/antigravity/brain/740e9642-4282-48dc-a8fd-527affef0850/uploaded_media_0_1769675626337.png',
        r'C:/Users/ecrea/.gemini/antigravity/brain/740e9642-4282-48dc-a8fd-527affef0850/uploaded_media_1_1769675626337.png'
    ]
    
    base_dir = 'src/main/resources/assets/elementmagicboss/textures/gui/boss_bars'
    
    for i, p in enumerate(paths):
        img = Image.open(p).convert('RGBA')
        w, h = img.size
        print(f"Image {i}: {w}x{h}")
        
        # Count purple/bright pixels
        purple_count = 0
        pixels = img.load()
        for y in range(h):
            for x in range(w):
                r, g, b, a = pixels[x, y]
                # Purple/Magenta check or just bright pixels that aren't black background
                if b > 100 and a > 50: 
                    purple_count += 1
        
        print(f"  Purple Pixel Count: {purple_count}")
        
        target_w = 256
        scale = target_w / w
        target_h = int(h * scale)
        img_scaled = img.resize((target_w, target_h), Image.Resampling.LANCZOS)
        
        # Store for comparison
        if i == 0:
            img0 = img_scaled
            count0 = purple_count
        else:
            img1 = img_scaled
            count1 = purple_count

    if count0 > count1:
        print("Image 0 is FULL, Image 1 is EMPTY")
        img0.save(f'{base_dir}/wizard_full.png')
        img1.save(f'{base_dir}/wizard_empty.png')
    else:
        print("Image 1 is FULL, Image 0 is EMPTY")
        img1.save(f'{base_dir}/wizard_full.png')
        img0.save(f'{base_dir}/wizard_empty.png')

    print(f"Saved scaled textures to {base_dir}")

if __name__ == '__main__':
    inspect_and_save()
