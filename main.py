from fastapi import FastAPI
import pyodbc
import os
# drivers = [driver for driver in pyodbc.drivers()]
# print(drivers)

app = FastAPI()

@app.get("/")
def home():
    return {"message":"Hello World"}



@app.get("/random_fact")
async def random_fact():
    fact = get_random_fact()
    if fact:
        return {"random_fact": fact}
    else:
        return {"error": "An error occurred while fetching the random fact."}


def get_random_fact():
    connection_string = os.getenv('WSconnectionString')
    
    if connection_string:
        try:
            cnxn = pyodbc.connect(connection_string)
            cursor = cnxn.cursor()
            cursor.execute("EXEC dbo.spRandomFact")
            record = cursor.fetchone()
            cnxn.commit()
            cursor.close()
            cnxn.close()
            if record:
                return {
                    #"factid":record[0],
                    "fact": record[1],
                    "primaryImage": record[2],
                    "secondaryImage": record[3]
                }
            else:
                return None
        except Exception as e:
            print(f"An error occurred: {e}")
            return str(e)
    else:
        print("Connection string not found in environment variables.")
        return None


@app.get("/random_fact")
async def random_fact():
    fact = get_random_fact()
    if fact:
        return {"random_fact": fact}
    else:
        return {"error": "An error occurred while fetching the random fact."}