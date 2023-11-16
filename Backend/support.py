from fastapi import FastAPI, UploadFile
import os
from PIL import Image
from roboflow import Roboflow
from sqlalchemy import create_engine, text
from fun_facts_data import fun_facts
import random


def get_animal_info(animal_name):
    conn = get_db_connection()
    if conn:
        try:
            query = text("EXEC ws.spGetAnimalInfo :name")
            
            result = conn.execute(query, {"name": animal_name})
            record = result.fetchone()

            if record:
                return {
                    "shortDescription": record[0],
                    "habitat": record[1],
                    "diet": record[2],
                    "location": record[3],
                    "type": record[4],
                    "lifeSpan": record[5],
                    "weight": record[6],
                    "top_speed": record[7]
                }
            else:
                return None
        except Exception as e:
            print(f"An error occurred: {e}")
            return str(e)
        finally:
            conn.close()
    else:
        print("Connection string not found in environment variables.")
        return None



def get_random_fact():
    if fun_facts:
        return random.choice(fun_facts)
    else:
        return None









def get_all_animals():
    conn = get_db_connection()
    if conn:
        try:
            query = text("EXEC ws.spGetAllAnimals")
            result = conn.execute(query)
            rows = result.fetchall()
            columns = result.keys()

            animals = [dict(zip(columns, row)) for row in rows]
            return animals
        except Exception as e:
            print(f"An error occurred: {e}")
            return []
        finally:
            conn.close()
    else:
        print("Connection string not found in environment variables.")
        return []

def get_favourite_animals(username):
    conn = get_db_connection()
    if conn:
        try:
            stmt = text("EXEC ws.spGetFavouriteAnimals :username")
            result = conn.execute(stmt, {"username": username})
            animals = result.fetchall()
            
            # Format the result
            formatted_result = {}
            for animal in animals:
                formatted_result[animal.category] = {
                    "shortDescription": animal.shortDescription,
                    "habitat": animal.habitat,
                    "diet": animal.diet,
                    "location": animal.location,
                    "type": animal.type,
                    "lifeSpan": animal.lifeSpan,
                    "weight": animal.weight,
                    "top_speed": animal.top_Speed
                }

            return formatted_result
        except Exception as e:
            print(f"An error occurred: {e}")
            return {}
        finally:
            conn.close()
    else:
        print("Connection string not found in environment variables.")
        return {}



def user_login(username, password):
    conn = get_db_connection()
    if conn:
        try:
            stmt = text("EXEC ws.spLogin :username, :password")
            stmt = stmt.bindparams(username=username, password=password)
            result = conn.execute(stmt)
            message = result.fetchone()[0]
            return message
        except Exception as e:
                return "Either the username doesn't exist, or the password doesn't match."
        finally:
            conn.close()
    else:
        return "Connection string not found in environment variables."

def create_account(username, email, password):
    conn = get_db_connection()
    if conn:
        try:
            stmt = text("EXEC ws.spCreateAccount :username, :email, :password")
            stmt = stmt.bindparams(username=username, email=email, password=password)
            result = conn.execute(stmt)
            message = result.fetchone()[0]

            # Commit the transaction
            conn.commit()

            return message
        except Exception as e:
            # Rollback in case of error
            conn.rollback()
            if "Username already exists." in str(e):
                return "Username already exists."
            elif "Email already exists." in str(e):
                return "Email already exists."
            else:
                return "An error occurred while creating the account."
        finally:
            conn.close()
    else:
        return "Connection string not found in environment variables."
    
def add_user_favourite(username, animal):
    conn = get_db_connection()
    if conn:
        try:
            stmt = text("EXEC ws.spUserFavouritesAnimal :username, :animal")
            stmt = stmt.bindparams(username=username, animal=animal)
            conn.execute(stmt)

            # Commit the transaction
            conn.commit()

            return "Favourite animal added successfully."
        except Exception as e:
            conn.rollback()
            error_message = str(e)
            if "50001" in error_message:
                return "User already favourited this animal."
            elif "50002" in error_message:
                return "User does not exist."
            elif "50003" in error_message:
                return "Animal doesn't exist."
            else:
                return "An error occurred while adding the favourite animal."
        finally:
            conn.close()
    else:
        return "Connection string not found in environment variables."

def classify_image_from_data(image_data, model):
    with open("temp_image.jpg", "wb") as f:
        f.write(image_data)

    full_prediction = model.predict("temp_image.jpg", confidence=40, overlap=30).json()

    if 'predictions' in full_prediction and full_prediction['predictions']:
        primary_prediction = full_prediction["predictions"][0]
        simplified_prediction = {
            "class": primary_prediction["class"],
            "confidence": round(primary_prediction["confidence"], 3)
        }
        return {"prediction": simplified_prediction}
    else:
        return {"error": "No predictions found in the response"}
    
def get_db_connection():
    engine = create_engine(os.getenv('WSconnectionString'))
    return engine.connect()