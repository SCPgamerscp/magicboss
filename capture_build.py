import subprocess

def run():
    try:
        result = subprocess.run(['gradlew.bat', 'compileJava', '--console=plain'], capture_output=True, text=True, check=False)
        with open('build_output_utf8.txt', 'w', encoding='utf-8') as f:
            f.write("STDOUT:\n")
            f.write(result.stdout)
            f.write("\nSTDERR:\n")
            f.write(result.stderr)
        print("Build output saved to build_output_utf8.txt")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    run()
