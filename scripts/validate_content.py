#!/usr/bin/env python3
"""Validate the bundled curriculum without requiring Android or third-party packages."""
import json
from pathlib import Path

content = json.loads((Path(__file__).resolve().parents[1] / 'data/src/main/assets/content.json').read_text())
for table in ('paths', 'chapters', 'lessons', 'questions', 'challenges', 'achievements'):
    ids = [row['id'] for row in content[table]]
    assert len(ids) == len(set(ids)), f'Duplicate IDs in {table}'
paths = {p['id'] for p in content['paths']}
chapters = {c['id'] for c in content['chapters']}
assert all(c['pathId'] in paths for c in content['chapters'])
assert len(content['lessons']) >= 24
assert len(content['questions']) >= 40
for lesson in content['lessons']:
    assert lesson['chapterId'] in chapters
    assert lesson['minutes'] > 0
    assert {'text', 'code', 'exercise'} <= {b['kind'] for b in lesson['blocks']}
    assert all(b['text'].strip() for b in lesson['blocks'])
for question in content['questions']:
    assert question['pathId'] in paths
    assert 0 <= question['correct'] < len(question['options'])
    assert len(set(question['options'])) == len(question['options'])
    assert question['explanation'].strip()
print(f"Validated {len(paths)} paths, {len(content['lessons'])} lessons, {len(content['questions'])} questions, and {len(content['challenges'])} coding challenges.")
