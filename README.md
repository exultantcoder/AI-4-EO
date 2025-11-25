# 🌍 Interactive offline AI Tutor: Gemma 3N on Google AI Edge

**No internet. No limits. AI for everyone, everywhere.**
This project was submitted to the Gemma3N Impact challenge on Kaggle. 
---

## 📖 Project Vision
This project is about something very simple — and very powerful:  
✨ Giving every child, no matter where they are in the world, the chance to learn.  

We believe learning is not just about books and exams. It’s about **dreams, purpose, and access**.  
Technology can make that happen — but most AI solutions require internet, data, and heavy infrastructure that many regions just don’t have.  

That’s why we built an **personalized interactive learning module**, powered by the **Gemma 3N-e2b-it** and **Gemma 3N -e4b-it** models running on **Google AI Edge Gallery App**.  

---

## 💡 What We Built
- **An interactive custom task designed to be deployed on the official open source Google AI Edge Gallery repo by Google.**
- **An offline personalized learning platform that adapts to the user's language and interests specially for those in children in underserved areas.**
- **TalkToMe option enable the user to chat with the model via image, text and audio , all-in-one tab.**
- **Better user data management and track of user's progress while learning modules.**
- **Adaptive feedback loop to train the model based on user's preferences to adapt to the user.**
- **Interactive Scientific games for technical knowledge and making learning enjoyable.**

Instead of asking kids to adapt to the AI, we made the AI **adapt to them**.  
---<img width="1024" height="768" alt="1" src="https://github.com/user-attachments/assets/2fef13ff-5264-4f19-b321-3bf1e49e0a88" />
<img width="1024" height="768" alt="2" src="https://github.com/user-attachments/assets/d5ea7f8c-d25f-4d7b-b6fc-819d9bf5a608" />
<img width="1024" height="768" alt="3" src="https://github.com/user-attachments/assets/f76642ee-9df1-4487-9dac-5f772269c86c" />
<img width="1024" height="768" alt="2" src="https://github.com/user-attachments/assets/d953be33-87f0-48e2-a6da-ae1d34d909e0" />
<img width="1024" height="768" alt="1" src="https://github.com/user-attachments/assets/7406eb75-88f3-468f-9088-d2f8c2a8c058" />

---


## 🚀 Future Scope
- **Deploy on Google AI Edge devices** and benchmark real-world performance.  
- **Expand language support** for global accessibility.  
- **Improve adaptivity** by fine-tuning Gemma with local, private interaction logs.  
- **Enhance multimodality** with lightweight vision transformers.  
- **Partner with NGOs** to bring this system to classrooms worldwide.  
- **Preloaded essential learning modules (STEM, environment, health, ethics).    



# Workflow :
1. Clone the Google AI edge gallery github repo
2. Add these interactive learning task files under the custom task folder and run the interactive module.

# Performance Metrics - Raspberry Pi 5 Deployment
This application is designed for deployment on single-board computers to enable scalable, cost-effective distribution of on-device AI capabilities. For this evaluation, we deployed the Interactive Learning application on a Raspberry Pi 5 (4GB RAM), a compact single-board computer suitable for mass deployment in educational environments, particularly in underserved regions requiring free access to quality learning resources.

# Deployment Methodology

1. The Android application was installed on the Raspberry Pi 5 by flashing Android OS (LineageOS 22) onto a 128GB microSD card. Another attempt was to use the waydroid The deployment process involved:

2. Flashing the Android OS image to the microSD card

3. Booting the Raspberry Pi 5 with Android OS

4. Installing the application APK (app-debug.apk)

5. Downloading the Gemma 3N-E2B-it  model from Hugging Face within the app

Metrics : The model running on a Raspberry Pi 5 with 4gb RAM 

<img width="337" height="167" alt="Screenshot 2025-11-25 at 3 20 58 PM" src="https://github.com/user-attachments/assets/b5c1f8c2-3d23-421f-b4d6-69cb5523bf74" />



Google AI Edge Gallery. (2025). An open-source Android application for running AI models locally on edge devices. Google AI Edge. Retrieved October 15, 2025, from https://github.com/google-ai-edge/gallery
