from PIL import Image
import os

def inspect_images():
    paths = [
        r'C:/Users/ecrea/.gemini/antigravity/brain/740e9642-4282-48dc-a8fd-527affef0850/uploaded_media_0_1769675018523.png',
        r'C:/Users/ecrea/.gemini/antigravity/brain/740e9642-4282-48dc-a8fd-527affef0850/uploaded_media_1_1769675018523.png',
        r'C:/Users/ecrea/.gemini/antigravity/brain/740e9642-4282-48dc-a8fd-527affef0850/uploaded_media_2_1769675018523.png'
    ]
    
    for i, p in enumerate(paths):
        try:
            img = Image.open(p).convert('RGBA')
            w, h = img.size
            
            # Count purple pixels
            purple_count = 0
            pixels = img.load()
            # Sample center area for fill
            for y in range(h // 3, h * 2 // 3):
                for x in range(w // 4, w * 3 // 4):
                    r, g, b, a = pixels[x, y]
                    if a > 50 and b > 100 and b > g:
                        purple_count += 1
            
            print(f"Image {i}: {w}x{h}, Purple Pixels (Center): {purple_count}")
            
            # Basic identification
            if purple_count > 1000:
                print(f"  -> Likely FULL state")
            else:
                print(f"  -> Likely EMPTY state")
                
        except Exception as e:
            print(f"Image {i}: Failed to open ({e})")

if __name__ == '__main__':
    inspect_images()
