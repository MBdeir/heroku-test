from fastapi import FastAPI, UploadFile
from PIL import Image
from support import get_animal_info,get_random_fact,classify_image_from_data,get_db_connection
from roboflow import Roboflow


app = FastAPI()

rf = Roboflow(api_key="08ygxtxy7JYwrrNijLPb")
project = rf.workspace().project("animals-ij5d2")
model = project.version(1).model

@app.get("/random_fact")
async def random_fact():
    fact = get_random_fact()
    if fact:
        return {"random_fact": fact}
    else:
        return {"error": "An error occurred while fetching the random fact."}

@app.post("/api/uploadImg")
async def classify_image_endpoint(file: UploadFile):
    image_data = await file.read()
    return classify_image_from_data(image_data, model)
    

@app.get("/animal_info")
async def animal_info(name: str):
    animal_info = get_animal_info(name)
    if animal_info:
        return { name : animal_info}
    else:
        return {"error":"It seems this animal is not in our database"}
    

@app.get("/all_animals")
async def all_animals():
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("EXEC ws.spGetAllAnimals")
    results = cursor.fetchall()
    columns = [column[0] for column in cursor.description]
    cursor.close()
    conn.close()
    animals = [dict(zip(columns, row)) for row in results]
    return animals





########################################CODE ARCHIEVE########################################
########################################CODE ARCHIEVE########################################
########################################CODE ARCHIEVE########################################
########################################CODE ARCHIEVE########################################
########################################CODE ARCHIEVE########################################
########################################CODE ARCHIEVE########################################
########################################CODE ARCHIEVE########################################
    


# from tensorflow.keras.applications.efficientnet import EfficientNetB7
# from tensorflow.keras.applications.efficientnet import decode_predictions
# drivers = [driver for driver in pyodbc.drivers()]
# print(drivers)
# @app.post("/api/uploadImg")
# async def name(file : UploadFile):
#     image = Image.open(io.BytesIO(await file.read()))
#     image = image.resize((600, 600))
#     image_np = np.array(image)
#     image_np = image_np.reshape(1, 600, 600, 3)
#     result = model.predict(image_np)
#     decoded_predictions = decode_predictions(result, top=1)[0]
#     predicted_class = decoded_predictions[0][1]
#     return {"category": predicted_class}


# @app.post("/api/uploadImg2")
# async def classify_image(file: UploadFile):
#     image_data = await file.read()
#     with open("temp_image.jpg", "wb") as f:
#         f.write(image_data)
    
#     prediction = model.predict("temp_image.jpg", confidence=40, overlap=30).json()
#     return {"prediction": prediction}

    
