# -*- coding: utf-8 -*-
"""
编码检查工具 - 确保所有源码文件均为 UTF-8 编码。
在提交代码前运行此脚本，防止 GBK/GB2312 等编码文件混入工程。

用法: python check_all_encoding.py
"""

import os

# 项目根目录
ROOT = os.path.dirname(os.path.abspath(__file__))

# 要检查的文件扩展名（纯二进制文件跳过）
TEXT_EXTENSIONS = {
    '.java', '.kt', '.kts', '.xml', '.gradle', '.properties',
    '.pro', '.txt', '.md', '.py', '.yml', '.yaml', '.json',
    '.bat', '.sh', '.cfg', '.toml', '.html', '.css', '.js',
    '.gitignore', '.gitattributes',
}

# 要跳过的目录
SKIP_DIRS = {
    '.git', '.gradle', 'build', '.idea', '.m2', 'node_modules',
    '.settings', 'intermediates', 'outputs', 'generated',
}

# 常见的乱码字符（GBK→UTF-8 解码错误产生的莫吉托）
# 注意：只包含不可能在正常中文文本中出现的乱码组合
MOJIBAKE_CHARS = {
    '\u93c5',  # 鏅 - GBK 0xD6C7(智) 被误读为 UTF-8
    '\u7481',  # 璁 - GBK 0xC4DC(能) 被误读为 UTF-8
    '\u9225',  # 鈥 - GBK 0xBCC7(记) 被误读为 UTF-8
    '\u7ff9',  # 鐜 - 常见乱码
}


def is_text_file(filename):
    """判断是否为文本文件"""
    _, ext = os.path.splitext(filename)
    return ext.lower() in TEXT_EXTENSIONS or ext == ''


def check_encoding(filepath):
    """
    检查文件编码。
    返回: (is_ok, message)
    """
    with open(filepath, 'rb') as f:
        data = f.read()

    if len(data) == 0:
        return True, "empty"

    # 跳过明显是二进制文件的（含空字节）
    if b'\x00' in data[:500]:
        return True, "binary"

    try:
        text = data.decode('utf-8')
    except UnicodeDecodeError:
        # 尝试检测是否为 GBK
        try:
            data.decode('gbk')
            return False, "GBK编码 (应转为UTF-8)"
        except UnicodeDecodeError:
            return False, "非UTF-8编码 (且非GBK)"

    # 检查是否有乱码字符（跳过本脚本自身和AGENTS.md/CLAUDE.md中的定义）
    filename = os.path.basename(filepath)
    if any(b > 127 for b in data) and filename not in ('check_all_encoding.py', 'ENCODING.md'):
        for gar in MOJIBAKE_CHARS:
            if gar in text:
                lines = text.split('\n')
                for i, line in enumerate(lines):
                    if gar in line:
                        return False, f"含乱码字符 (行 {i + 1})"

    return True, "OK"


def main():
    errors = []
    checked = 0

    for dirpath, dirnames, filenames in os.walk(ROOT):
        # 跳过构建目录等
        dirnames[:] = [d for d in dirnames if d not in SKIP_DIRS]

        for fname in filenames:
            if not is_text_file(fname):
                continue

            filepath = os.path.join(dirpath, fname)
            relpath = os.path.relpath(filepath, ROOT)

            ok, msg = check_encoding(filepath)
            checked += 1

            if not ok:
                errors.append((relpath, msg))
                print(f'FAIL: {relpath}  ->  {msg}')

    print(f'\n扫描完成：共检查 {checked} 个文件')

    if errors:
        print(f'发现 {len(errors)} 个编码问题！')
        print('请修复后重新运行此脚本。')
        exit(1)
    else:
        print('所有文件均为正确的 UTF-8 编码。')
        exit(0)


if __name__ == '__main__':
    main()
