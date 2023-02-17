# Tubes1_PlayerNumberOne
Tugas Besar 1 IF2211 – Strategi Algoritma Tahun 2022/2023

## Table of Contents
* [General Info](#general-information)
* [Technologies Used](#technologies-used)
* [Penjelasan Singkat Algoritma](#Penjelasan-Singkat-Algoritma)
* [Features](#features)
* [Screenshots](#screenshots)
* [Setup](#cara-menjalankan-program)
* [Project Status](#project-status)
* [Room for Improvement](#room-for-improvement)
* [Pembagian](#pembagian-tugas)


## General Information
Galaxio adalah sebuah game battle royale yang mempertandingkan bot kapal anda dengan beberapa bot kapal yang lain. Setiap pemain akan memiliki sebuah bot kapal dan tujuan dari permainan adalah agar bot kapal anda yang tetap hidup hingga akhir permainan. Penjelasan lebih lanjut mengenai aturan permainan akan dijelaskan di bawah. Agar dapat memenangkan pertandingan, setiap bot harus mengimplementasikan strategi tertentu untuk dapat memenangkan permainan.


## Technologies Used
- java (minimal java 11)
- .Net Core 5.0 dan 3.0 
- Maven (hanya untuk build source code)


## Penjelasan Singkat Algoritma
* List semua kebutuhan objek berdasarkan jenisnya kemudian diurutkan berdasarkan jarak terdekat dengan kami atau meminimisasi-nya sesuai dengan algoritma greedy. Untuk smallestFood dan smallestPlayer diberikan fungsi seleksi tambahan yaitu validasi apakah food atau player tersebut berada dalam gas cloud dan apakah berada di dekat border dari permainan.
* Inisiasi awal dari batasan-batasan seperti jarak dan ukuran.
* Membagi 3 kondisi utama sebagai dasar utama sebagai fungsi solusi yang akan dipilih untuk aksi berikutnya yaitu: 
    - Firing supernova jika memilikinya
    - Passive mode : bot bertindak lebih utama untuk mencari makanan 
    - Active mode : bot lebih aktif untuk menyerang ke lawan
* Supernova adalah langkah pertimbangan pertama yang dilakukan oleh bot. Jika bot memiliki supernova, ia akan menembakkannya ke arah player dengan jarak terdekat daripadanya.
* Passive mode merupakan kondisi saat ukuran dari bot lebih besar dari player lawan terdekat. Passive mode mempertimbangkan jarak yang terbagi menjadi :
    - Jarak bot dengan lawan terlalu dekat, kondisi ini terbagi menjadi 2 kegiatan lainnya yaitu:
        - Jika bot memiliki torpedo, bot akan menembakkan torpedo ke arah lawan sambil menjauh
        - Jika tidak memiliki torpedo, bot hanya akan menjauh saja
    - Jarak bot dengan lawan tidak terlalu dekat, bot akan mencari makanan
- Active mode merupakan kondisi saat ukuran dari bot lebih besar dari player lawan terdekat. Active mode mempertimbangkan banyak alat menyerang yang dimiliki oleh bot, yaitu:
    - Jika bot memiliki peluru teleporter dan ukuran bot setelah menembakkan teleporter tidak lebih kecil dari player yang dituju, bot akan menembakkan teleporter ke arah lawan tersebut.
    - Jika bot tidak memiliki teleporter tetapi memiliki torpedo dan jaraknya sudah sesuai, bot akan menembakkan torpedonya ke arah lawan
    - Jika tidak memliki keduanya, maka:
        - Jika jarak sudah dekat, maka bot akan mengejar lawan
        - Jika jarak terlalu jauh, maka bot akan mencari makanan
- Setelah melakukan evaluasi utama tersebut, bot akan mengevaluasi detonasi teleporter. Jika pemain terdekat memiliki jarak yang dekat dengan sebuah teleporter, maka bot akan melakukan detonasi pada teleporter (walaupun teleport yang dekat bukan bukan milik bot).
- Bot kemudian mengevaluasi detonasi supernova. Jika supernova yang berada di map memiliki jarak yang dekat dengan player terdekat dari bot, bot akan meledakkan supernova.
- Diluar semua kondisi tersebut, ketika objek lain selain player sudah tidak ada, bot akan mengarah ke tengah map.



## Features
- Implementasi dari algoritma greedy pada kode program bot PlayerNumberOne
- Optimalisasi algoritma dengan pendekatan heuristic


## Screenshots
![Example screenshot](./doc/galaxio.png)


## Cara menjalankan program 
Untuk build program dapat dilakukan dengan : 
- command "mvn clean package" (pada root direktori dari repositori)
Berikut merupakan cara menjalankan game secara lokal di Windows:
1. Lakukan konfigurasi jumlah bot yang ingin dimainkan pada file JSON ”appsettings.json” dalam folder “runner-publish” dan“engine-publish”
2. Buka terminal baru pada folder runner-publish.
3. Jalankan runner menggunakan perintah “dotnet GameRunner.dll”
4. Buka terminal baru pada folder engine-publish
5. Jalankan engine menggunakan perintah “dotnet Engine.dll”
6. Buka terminal baru pada folder logger-publish
7. Jalankan engine menggunakan perintah “dotnet Logger.dll”
8. Jalankan seluruh bot yang ingin dimainkan
9. Setelah permainan selesai, riwayat permainan akan tersimpan pada 2 file JSON “GameStateLog_{Timestamp}” dalam folder “logger-publish”. Kedua file tersebut diantaranya GameComplete (hasil akhir dari permainan) dan proses dalam permainan tersebut.

## Project Status
Project is: _complete_ 


## Room for Improvement

Room for improvement:
- speed up for algorithm
- algorithm optimization


## Pembagian Tugas
1. Nigel Sahl (13521043)                : brainstorming, membuat alternatif solusi dari algoritma greedy, menulis laporan 
2. Hanif Muhammad Zhafran (13521157)    : brainstorming, membuat alternatif solusi dari algoritma greedy, menulis laporan 
3. Hosea Nathanael Abetnego (13521057)  : brainstorming, membuat alternatif solusi dari algoritma greedy, menulis laporan 