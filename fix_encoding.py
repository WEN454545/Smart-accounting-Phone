#!/usr/bin/env python3
"""
Encoding fix tool - Convert all non-UTF-8 project files to UTF-8.
Usage:
    python fix_encoding.py                    # scan & fix entire project
    python fix_encoding.py <relative_path>    # fix a single file
"""

import os
import sys


def has_chinese(text):
    return sum(1 for c in text if '\u4e00' <= c <= '\u9fff' or '\u3400' <= c <= '\u4dbf')


def looks_like_valid_chinese(text):
    cn = has_chinese(text)
    if cn == 0:
        return False
    common = set('\u7684\u4e86\u662f\u4e0d\u5728\u6709\u4eba\u6211\u4ed6\u8fd9\u4e2d\u5927\u6765\u4e0a\u56fd\u4e2a\u5230')
    return sum(1 for c in common if c in text) >= 3


def is_non_ascii_mojibake(text):
    """Check if text has non-ASCII, non-CJK chars (likely mojibake)."""
    count = 0
    for c in text:
        cp = ord(c)
        if cp < 128:                    # ASCII
            continue
        if 0x4E00 <= cp <= 0x9FFF:     # CJK Unified
            continue
        if 0x3400 <= cp <= 0x4DBF:     # CJK Extension A
            continue
        if 0x3000 <= cp <= 0x30FF:     # CJK punctuation, kana
            continue
        if 0xFF00 <= cp <= 0xFFEF:     # Fullwidth
            continue
        if 0x2000 <= cp <= 0x206F:     # General punctuation
            continue
        count += 1
    return count > 30


def try_recover(text_utf8):
    """Try multiple strategies to recover original Chinese from mojibake UTF-8 text.
    Returns (recovered_text, strategy_name) or (None, None)."""
    
    strategies = [
        # Strategy 1: direct GBK decode of raw bytes
        lambda: text_utf8.encode('latin-1', errors='replace').decode('gbk', errors='replace'),
        # Strategy 2: cp1252 roundtrip
        lambda: text_utf8.encode('cp1252', errors='replace').decode('gbk', errors='replace'),
        # Strategy 3: iso-8859-15 roundtrip
        lambda: text_utf8.encode('iso-8859-15', errors='replace').decode('gbk', errors='replace'),
        # Strategy 4: mac_roman roundtrip
        lambda: text_utf8.encode('mac_roman', errors='replace').decode('gbk', errors='replace'),
        # Strategy 5: mixed approach - try to preserve ASCII while recovering non-ASCII
        lambda: recover_mixed(text_utf8),
    ]
    
    for i, strategy in enumerate(strategies):
        try:
            result = strategy()
            if looks_like_valid_chinese(result):
                return result, f"strategy_{i+1}"
        except Exception:
            continue
    
    return None, None


def recover_mixed(text):
    """Mixed recovery: keep ASCII, try to recover non-ASCII parts."""
    result = []
    non_ascii_buf = []
    
    for c in text:
        if ord(c) < 128:
            # Process buffered non-ASCII and then add this ASCII char
            if non_ascii_buf:
                result.extend(recover_chunk(''.join(non_ascii_buf)))
                non_ascii_buf = []
            result.append(c)
        else:
            non_ascii_buf.append(c)
    
    if non_ascii_buf:
        result.extend(recover_chunk(''.join(non_ascii_buf)))
    
    return ''.join(result)


def recover_chunk(chunk):
    """Try to recover a chunk of mojibake text."""
    for encoding in ['latin-1', 'cp1252', 'iso-8859-15', 'mac_roman']:
        try:
            recovered = chunk.encode(encoding, errors='replace').decode('gbk', errors='replace')
            if has_chinese(recovered) > 0:
                return [recovered]
        except Exception:
            continue
    return [chunk]  # Can't recover, keep original


def try_fix_file(filepath):
    """Try to convert file to UTF-8. Returns True if fixed."""
    with open(filepath, 'rb') as f:
        raw = f.read()

    if not raw:
        return False

    # Step 1: Try UTF-8
    try:
        text_utf8 = raw.decode('utf-8')
    except UnicodeDecodeError:
        text_utf8 = None

    if text_utf8 is not None:
        # Already correct UTF-8 Chinese? Skip.
        if looks_like_valid_chinese(text_utf8):
            return False
        
        # Check for mojibake
        if not is_non_ascii_mojibake(text_utf8):
            return False  # Probably a clean file without Chinese
        
        # Try recovery strategies
        recovered, strategy = try_recover(text_utf8)
        if recovered is not None:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(recovered)
            print(f"  Recovered via {strategy}")
            return True
        
        return False
    
    # Step 2: Not UTF-8, try GBK
    try:
        text = raw.decode('gbk')
        if looks_like_valid_chinese(text):
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(text)
            return True
    except UnicodeDecodeError:
        pass

    return False


def fix_one(filepath):
    rel = os.path.relpath(filepath, os.path.dirname(os.path.abspath(__file__)))
    print(f"Processing: {rel}")
    ok = try_fix_file(filepath)
    if ok:
        print(f"  FIXED: {rel}")
    return ok


def scan_all(root):
    skip = {'.git', '.gradle', 'build', '.idea', '.m2', 'node_modules',
            '.settings', 'intermediates', 'outputs', 'generated'}
    exts = {'.java', '.xml', '.gradle', '.properties', '.pro', '.txt', '.md', '.py'}
    fixed = []
    checked = 0

    for dirpath, dirnames, filenames in os.walk(root):
        dirnames[:] = [d for d in dirnames if d not in skip]
        for fname in filenames:
            ext = os.path.splitext(fname)[1].lower()
            if ext not in exts:
                continue
            filepath = os.path.join(dirpath, fname)
            checked += 1
            if try_fix_file(filepath):
                fixed.append(os.path.relpath(filepath, root))
                print(f"FIXED: {os.path.relpath(filepath, root)}")

    print(f"\nDone. Checked {checked}, fixed {len(fixed)}.")
    return len(fixed)


if __name__ == '__main__':
    root = os.path.dirname(os.path.abspath(__file__))
    args = sys.argv[1:]

    if args:
        target = args[0]
        if not os.path.isabs(target):
            target = os.path.join(root, target)
        if os.path.isfile(target):
            fix_one(target)
        else:
            print(f"File not found: {target}")
    else:
        scan_all(root)
