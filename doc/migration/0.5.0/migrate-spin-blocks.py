#!/usr/bin/env python3

import re
import sys
import argparse
import os
from pathlib import Path

def remove_sp_blocks(content):
    """
    Remove sp:where and sp:templates blocks from TTL content.
    Also removes rdfs:comment when sibling of rdf:type/a with sp:Ask/sp:Select/sp:Construct.
    Handles nested parentheses properly using a more robust approach.
    """
    lines = content.split('\n')
    result_lines = []
    i = 0
    
    while i < len(lines):
        line = lines[i]
        stripped = line.strip()
        
        # Check for sp:where or sp:templates blocks (with or without opening parenthesis)
        if (stripped == 'sp:where' or stripped.startswith('sp:where (') or 
            stripped == 'sp:templates' or stripped.startswith('sp:templates (')):
            
            # Find the opening parenthesis if it's on the same line
            paren_depth = line.count('(') - line.count(')')
            i += 1
            
            # Continue reading lines until we close all parentheses
            while i < len(lines) and paren_depth > 0:
                current_line = lines[i]
                paren_depth += current_line.count('(') - current_line.count(')')
                i += 1
            
            # After closing sp:where/sp:templates block, check for rdfs:comment
            # Skip any rdfs:comment lines that are siblings
            while i < len(lines):
                next_line = lines[i].strip()
                if next_line.startswith('rdfs:comment'):
                    i += 1
                elif next_line == ';' or next_line == '];' or next_line == ']':
                    i += 1
                    break
                else:
                    break
            
            continue
        
        # Check for rdfs:comment after sp:Ask/sp:Select/sp:Construct blocks
        if (stripped.startswith('rdf:type sp:Ask') or 
            stripped.startswith('rdf:type sp:Select') or 
            stripped.startswith('rdf:type sp:Construct') or
            stripped.startswith('a sp:Ask') or
            stripped.startswith('a sp:Select') or
            stripped.startswith('a sp:Construct')):
            
            # Add the current line
            result_lines.append(line)
            i += 1
            
            # Look ahead for rdfs:comment and skip it
            while i < len(lines):
                next_line = lines[i].strip()
                if next_line.startswith('rdfs:comment'):
                    i += 1
                elif next_line == ';' or next_line.startswith('sp:'):
                    break
                else:
                    break
            continue
        
        # If not in a block to be removed, keep the line
        result_lines.append(line)
        i += 1
    
    return '\n'.join(result_lines)

def remove_sp_blocks_regex(content):
    """
    Alternative approach using regex to remove sp:where and sp:templates blocks.
    This handles the common pattern where blocks are followed by parenthesized content.
    """
    # Remove sp:where blocks (including the following parenthesized content)
    content = re.sub(r'sp:where\s*\n\s*\([^)]*\)', '', content, flags=re.MULTILINE | re.DOTALL)
    
    # Remove sp:templates blocks (including the following parenthesized content)
    content = re.sub(r'sp:templates\s*\n\s*\([^)]*\)', '', content, flags=re.MULTILINE | re.DOTALL)
    
    # Handle multi-line blocks with nested parentheses
    def remove_block(block_name):
        pattern = rf'{block_name}\s*\n\s*\((?:[^()]|\([^()]*\))*\)'
        return re.sub(pattern, '', content, flags=re.MULTILINE | re.DOTALL)
    
    content = remove_block('sp:where')
    content = remove_block('sp:templates')
    
    return content

def process_file(input_file, output_file=None):
    """
    Process a TTL file to remove sp:where and sp:templates blocks.
    """
    input_path = Path(input_file)
    
    if not input_path.exists():
        print(f"Error: Input file '{input_file}' not found")
        return False
    
    # Read the input file
    try:
        with open(input_path, 'r', encoding='utf-8') as f:
            content = f.read()
    except Exception as e:
        print(f"Error reading file '{input_file}': {e}")
        return False
    
    # Process the content
    processed_content = remove_sp_blocks(content)
    
    # Determine output file (default to input file for in-place modification)
    if output_file is None:
        output_path = input_path
    else:
        output_path = Path(output_file)
    
    # Write the processed content
    try:
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(processed_content)
        print(f"Processed file saved as: {output_path}")
        print("Removed sp:where and sp:templates blocks")
        return True
    except Exception as e:
        print(f"Error writing to file '{output_path}': {e}")
        return False

def main():
    parser = argparse.ArgumentParser(
        description='Remove sp:where and sp:templates blocks from TTL files'
    )
    parser.add_argument('input_file', help='Input TTL file')
    parser.add_argument('output_file', nargs='?', help='Output TTL file (optional)')
    parser.add_argument('--regex', action='store_true', 
                       help='Use regex-based removal (alternative method)')
    
    args = parser.parse_args()
    
    if args.regex:
        # Override the processing function to use regex
        def process_file_regex(input_file, output_file=None):
            input_path = Path(input_file)
            
            if not input_path.exists():
                print(f"Error: Input file '{input_file}' not found")
                return False
            
            try:
                with open(input_path, 'r', encoding='utf-8') as f:
                    content = f.read()
            except Exception as e:
                print(f"Error reading file '{input_file}': {e}")
                return False
            
            processed_content = remove_sp_blocks_regex(content)
            
            if output_file is None:
                output_path = input_path
            else:
                output_path = Path(output_file)
            
            try:
                with open(output_path, 'w', encoding='utf-8') as f:
                    f.write(processed_content)
                print(f"Processed file saved as: {output_path}")
                print("Removed sp:where and sp:templates blocks (using regex)")
                return True
            except Exception as e:
                print(f"Error writing to file '{output_path}': {e}")
                return False
        
        success = process_file_regex(args.input_file, args.output_file)
    else:
        success = process_file(args.input_file, args.output_file)
    
    sys.exit(0 if success else 1)

if __name__ == '__main__':
    main()
