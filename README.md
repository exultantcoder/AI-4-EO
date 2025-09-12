# 🌍 Offline AI Tutor: Gemma 3N on Google AI Edge

**No internet. No limits. AI for everyone, everywhere.**

---

## 📖 Project Vision
This project is about something very simple — and very powerful:  
✨ Giving every child, no matter where they are in the world, the chance to learn.  

We believe learning is not just about books and exams. It’s about **dreams, purpose, and access**.  
Technology can make that happen — but most AI solutions require internet, data, and heavy infrastructure that many regions just don’t have.  

That’s why we built an **offline AI tutor**, powered by the **Gemma 3N-e2b-it** model running on **Google AI Edge**.  

---

## 💡 What We Built
- **A local learning engine** that interacts with the student and adapts questions, difficulty, and encouragement style based on their responses.  
- **On-device Gemma 3N (quantized)** for lightweight, offline inference.  
- **Multimodal support**: voice-to-text, text understanding, and basic image recognition — all offline.  
- **Adaptive feedback loop** to personalize the learning experience for every child.  

Instead of asking kids to adapt to the AI, we made the AI **adapt to them**.  

---

## 🛠️ Technical Overview
### 🔹 Model Selection
We chose **Gemma 3N-E2B-it** because of its small size (~2B parameters), efficient mixture-of-experts routing, and suitability for adaptive tasks.



### 🔹 Deployment Compatibility
- Target devices: **Raspberry Pi 5**, **Coral Dev Board**, and **Google AI Edge SDK** (Android/iOS).  
- Designed for **offline first**: once installed, no internet is required.

### 🔹 On-Device Capabilities
- **Voice, text, and image input**  
- **Adaptive learning loop** for personalized tutoring  
- **Efficient memory and power usage**  

---

## 🌍 Why This Matters
Every child has a voice. For many, this AI may be the **first time they feel heard and guided**.  

- A child in **South Sudan** dreaming of becoming a scientist.  
- A boy in a **remote Philippine village** hoping to lead disaster relief.  
- A girl in **rural Mexico** determined to fight environmental pollution.  

This system can hear them, respond in their local language, and guide them forward — **offline, without limits**.

---

## 🚀 Future Scope
- **Deploy on Google AI Edge devices** and benchmark real-world performance.  
- **Expand language support** for global accessibility.  
- **Improve adaptivity** by fine-tuning Gemma with local, private interaction logs.  
- **Enhance multimodality** with lightweight vision transformers.  
- **Partner with NGOs** to bring this system to classrooms worldwide.  

---

## 🤝 Challenges We Faced
- Personalizing content without cloud storage.  
- Staying within power and memory limits on small devices.  

### How We Overcame Them
- Built an interactive learning module as part of the custom task in the google ai edge gallery app.  
- Preloaded essential learning modules (STEM, environment, health, ethics).    

---

# Workflow :
1. Clone the Google AI edge gallery github repo
2. Add these four files under the custom task folder and run the interactive module. 
