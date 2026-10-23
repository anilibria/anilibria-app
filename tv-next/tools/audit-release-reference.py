"""Read-only DOM inventory of an exported release page; never executes scripts."""
import sys, zipfile, json, re
from pathlib import Path
from html.parser import HTMLParser
from collections import Counter
archive=Path(sys.argv[1]); out=Path(sys.argv[2]);out.mkdir(parents=True,exist_ok=True)
z=zipfile.ZipFile(archive)
name=next(n for n in z.namelist() if n.endswith('.html') and '/' not in n)
class Parser(HTMLParser):
 def __init__(self):super().__init__();self.nodes=[];self.stack=[]
 def handle_starttag(self,tag,attrs):
  original=dict(attrs)
  attrs={k:v for k,v in original.items() if k in {'class','style','role','tabindex','disabled','aria-selected','aria-disabled','type','width','height'}}
  n=dict(tag=tag,attrs=attrs,parent=self.stack[-1] if self.stack else None,children=[],text='')
  index=len(self.nodes);self.nodes.append(n)
  if self.stack:self.nodes[self.stack[-1]]['children'].append(index)
  if tag not in {'area','base','br','col','embed','hr','img','input','link','meta','param','source','track','wbr'}:self.stack.append(index)
 def handle_endtag(self,tag):
  for i in range(len(self.stack)-1,-1,-1):
   if self.nodes[self.stack[i]]['tag']==tag:self.stack=self.stack[:i];break
 def handle_data(self,text):
  if self.stack and any(self.nodes[i]['tag']=='main' for i in self.stack) and self.nodes[self.stack[-1]]['tag'] not in {'script','style'}:
   self.nodes[self.stack[-1]]['text']+=' '.join(text.split())
p=Parser();p.feed(z.read(name).decode('utf-8'))
(out/'dom.json').write_text(json.dumps(p.nodes,ensure_ascii=False,indent=2))
classes=sorted({c for n in p.nodes for c in n['attrs'].get('class','').split()})
(out/'classes.json').write_text(json.dumps(classes,ensure_ascii=False,indent=2))
lines=['# Полный DOM страницы релиза','', 'Источник: сохранённый HTML из архива. Скрипты не выполнялись. Текст учётной записи и встроенные JSON-пакеты не включены.','',f'Всего открывающих тегов: {len(p.nodes)}. Каждый тег включён ниже; индексы соответствуют dom.json.','', '| № | Родитель | Тег | Классы и атрибуты | Прямой текст внутри main |','|---|---|---|---|---|']
for i,n in enumerate(p.nodes):
 def safe(s):return str(s).replace('|','\\|').replace('\n',' ')
 lines.append(f"| {i} | {n['parent']} | {n['tag']} | {safe(json.dumps(n['attrs'],ensure_ascii=False))} | {safe(n['text'])} |")
(out/'DOM-BY-TAG.md').write_text('\n'.join(lines))
cssdir=out/'css';cssdir.mkdir(exist_ok=True)
for n in z.namelist():
 if n.endswith('.css'): (cssdir/Path(n).name).write_bytes(z.read(n))
print(json.dumps({'tags':len(p.nodes),'counts':dict(Counter(n['tag'] for n in p.nodes)),'classes':len(classes),'css':len(list(cssdir.glob('*.css')))},ensure_ascii=False))
