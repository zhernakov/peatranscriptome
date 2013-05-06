import mysql.connector
from struct import *


host = "193.218.141.85"
port = "1981"
user = "alexander"
password = "1981ujlfhj;ltybz"
database = "peatranscriptome"

path = "taxonomy"

def getValue(ar):
        ba = bytearray([ar[2],ar[1],ar[0],0,ar[5],ar[4],ar[3],0])
        return unpack('II', ba)

f = open(path, 'rb')

count = 0 
while True:
        read = f.read(6)
        if not read: break
        #fr = f.read(3)
        #sr = f.read(3)
        #if not fr or not sr: break
        taxid, parent = getValue(read)
        #taxid = getValue(fr)
        #parent = getValue(sr)
        print(taxid, parent)
        count += 1

print (count)

connection = mysql.connector.connect(
        host=host,
        port=port,
        user=user,
        password=password,
        database=database)
 
cursor = connection.cursor()
 
cursor.execute("SELECT * FROM taxonomy")
 
for (id) in cursor:
	print (id)


connection.close();
