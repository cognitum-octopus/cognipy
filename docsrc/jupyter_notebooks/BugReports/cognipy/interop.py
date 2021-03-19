import json
import sys

import os
from subprocess import PIPE, Popen
import atexit

def find(name, path):
    for root, dirs, files in os.walk(path):
        if name in files:
            return os.path.join(root, name)


pth=find("CogniPyCLI.exe",os.path.dirname(__file__))

cognipy_p=Popen([pth] if sys.platform.startswith('win32') else ['mono',pth],stdin=PIPE,stdout=PIPE,cwd=os.getcwd())

def cognipy_create():
    global cognipy_p
    cognipy_p.stdin.write("@create\r\n".encode())
    cognipy_p.stdin.flush()
    id= cognipy_p.stdout.readline().decode().strip()
    cognipy_p.stdout.readline()
    return id

def cognipy_delete(uid):
    global cognipy_p
    cognipy_p.stdin.write(("@delete\r\n"+uid+"\r\n").encode())
    cognipy_p.stdin.flush()
    cognipy_p.stdout.readline()
    cognipy_p.stdout.readline()

@atexit.register
def cognipy_close():
    global cognipy_p
    cognipy_p.stdin.write(("@exit\r\n").encode())
    cognipy_p.stdin.flush()

class ParseException(Exception):
    pass

def cognipy_call(uid,cmd,*args):

    def translate_exception(edet):
        def filter_dic(dic):
            return { k:v for (k,v) in dic.items() if k in ['Line','Column','Pos','Context','Hint']}
            
        if edet[0]=='ParseException':
            return ParseException({"Errors":[filter_dic(edet[1])]})
        elif edet[0]=='AggregateParseException':
            return ParseException({"Errors":[
                    filter_dic(inner) for inner in edet[1]["InnerExceptions"]
                ]})
        elif edet[0]=='NotImplementedException':
            return ValueError(edet[1]["Message"])
        return Exception(js)

    global cognipy_p
    txt= cmd+"\r\n"+uid+"\r\n"+json.dumps(args)+"\r\n\0\r\n"
    cognipy_p.stdin.write((txt).encode())
    cognipy_p.stdin.flush()
    fl=cognipy_p.stdout.readline().decode()
    ja=[]
    for line in iter(cognipy_p.stdout.readline, ''):
        l = line.decode()
        if len(l)>0 and l[0]=='\0':
            break
        ja.append(l)
    js="\r\n".join(ja)
    if fl.strip()=='@exception':
        raise translate_exception(json.loads(js))
    return json.loads(js)

