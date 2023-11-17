from fastapi import FastAPI, HTTPException, Query, UploadFile
from PIL import Image
from pydantic import BaseModel
from support import get_animal_info,classify_image_from_data, get_random_fact,get_all_animals,create_account,user_login,add_user_favourite,get_favourite_animals
from roboflow import Roboflow
from sqlalchemy import create_engine, text
from fun_facts_data import fun_facts  

app = FastAPI()

rf = Roboflow(api_key="08ygxtxy7JYwrrNijLPb")
project = rf.workspace().project("animals-ij5d2")
model = project.version(1).model


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=10000)


@app.get("/random_fact")
async def random_fact():
    fact = get_random_fact()
    if fact:
        return {"random_fact": fact}
    else:
        return {"error": "An error occurred while fetching the random fact."}

@app.post("/uploadImg")
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


class UserFavouritesRequest(BaseModel):
    username: str

@app.get("/fav_animals")
async def user_favourites(request: UserFavouritesRequest):
    favourites = get_favourite_animals(request.username)
    if favourites:
        return favourites
    else:
        raise HTTPException(status_code=404, detail="No favourites found or an error occurred.")


class LoginRequest(BaseModel):
    username: str
    password: str

@app.post("/login")
async def login_endpoint(request_data: LoginRequest):
    message = user_login(request_data.username, request_data.password)
    return {"message":message}


@app.get("/all_animals")
async def all_animals():
    animals = get_all_animals()
    if animals:
        return animals
    else:
        return {"error": "An error occurred while fetching all animals."}
    



class CreateAccountRequest(BaseModel):
    username: str
    email: str
    password: str


@app.post("/create_account")
async def create_account_endpoint(request_data: CreateAccountRequest):
    message = create_account(request_data.username, request_data.email, request_data.password)
    return {"message":message}

    
class FavouriteAnimalRequest(BaseModel):
    username: str
    animal: str

@app.post("/add_favourite_animal")
async def add_favourite_animal(request_data: FavouriteAnimalRequest):
    message = add_user_favourite(request_data.username, request_data.animal)
    
    if "successfully" in message:
        return {"message": message}
    else:
        raise HTTPException(status_code=400, detail=message)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=10000)


#hello world
