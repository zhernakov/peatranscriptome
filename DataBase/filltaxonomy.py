import mysql.connector
from struct import *

path = "../_data/taxonomy_rs"

host = "192.168.0.199"
port = "1981"
user = "alexander"
password = "1981ujlfhj;ltybz"
database = "peatranscriptome"


def getValue(ar):
        return unpack('II', bytearray([ar[2],ar[1],ar[0],0,ar[5],ar[4],ar[3],0]))

class Processor:
        def __init__(self, cnc):
                self.connection = cnc
                self.count = 0
                self.query = None
        def add(self, values):
                if not self.query:
                        self.query = 'INSERT INTO transcriptom VALUES '
                self.query += '(' + str(values[0]) + ',' + str(values[0]) + '),'
                self.count += 1
                if self.count > 100:
                        self.flush()
        def flush(self):
                if self.query:
                        print (self.query)
                        self.query = None
                        self.count = 0
                        
                

connection = mysql.connector.connect(
        host=host,
        port=port,
        user=user,
        password=password,
        database=database)

processor = Processor(connection)

f = open(path, 'rb')

count = 0 
while True:
        read = f.read(6)
        if not read:
                break
        processor.add(getValue(read))
        count += 1

print (count)

 
cursor = connection.cursor()
 
cursor.execute("SELECT * FROM taxonomy")
 
for (id) in cursor:
	print (id)

f.close
connection.close()
