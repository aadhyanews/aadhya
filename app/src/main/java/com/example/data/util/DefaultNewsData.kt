package com.example.data.util

import com.example.data.model.Article
import com.example.data.model.Category
import com.example.data.model.District
import com.example.data.model.VideoNews

object DefaultNewsData {

    val categories = listOf(
        Category("latest", "తాజా వార్తలు", "latest", true, 1),
        Category("breaking", "బ్రేకింగ్ న్యూస్", "breaking", true, 2),
        Category("telangana", "తెలంగాణ", "telangana", true, 3),
        Category("andhra", "ఆంధ్రప్రదేశ్", "andhra_pradesh", true, 4),
        Category("hyderabad", "హైదరాబాద్", "hyderabad", true, 5),
        Category("narayanpet", "నారాయణపేట", "narayanpet", true, 6),
        Category("mahabubnagar", "మహబూబ్ నగర్", "mahabubnagar", true, 7),
        Category("kodangal", "కొడంగల్", "kodangal", true, 8),
        Category("politics", "రాజకీయాలు", "politics", true, 9),
        Category("national", "జాతీయ వార్తలు", "national", true, 10),
        Category("international", "అంతర్జాతీయ వార్తలు", "international", true, 11),
        Category("crime", "క్రైమ్", "crime", true, 12),
        Category("education", "విద్య", "education", true, 13),
        Category("jobs", "ఉద్యోగాలు", "jobs", true, 14),
        Category("agriculture", "వ్యవసాయం", "agriculture", true, 15),
        Category("business", "వ్యాపారం", "business", true, 16),
        Category("sports", "క్రీడలు", "sports", true, 17),
        Category("cinema", "సినిమా", "cinema", true, 18),
        Category("health", "ఆరోగ్యం", "health", true, 19),
        Category("technology", "టెక్నాలజీ", "technology", true, 20)
    )

    val districts = listOf(
        District("narayanpet", "నారాయణపేట", true, 1),
        District("mahabubnagar", "మహబూబ్ నగర్", true, 2),
        District("kodangal", "కొడంగల్", true, 3),
        District("hyderabad", "హైదరాబాద్", true, 4),
        District("rangareddy", "రంగారెడ్డి", true, 5),
        District("warangal", "వరంగల్", true, 6),
        District("karimnagar", "కరీంనగర్", true, 7),
        District("khammam", "ఖమ్మం", true, 8),
        District("nizamabad", "నిజామాబాద్", true, 9),
        District("vijayawada", "విజయవాడ", true, 10),
        District("visakhapatnam", "విశాఖపట్నం", true, 11),
        District("tirupati", "తిరుపతి", true, 12)
    )

    val sampleArticles = listOf(
        Article(
            id = "art_1",
            titleTe = "తెలంగాణలో కొత్త సంక్షేమ పథకాల అమలు వేగవంతం: సీఎం కీలక ప్రకటన",
            contentTe = "హైదరాబాద్: రాష్ట్ర ప్రజల సంక్షేమమే ప్రథమ ప్రాధాన్యతగా ప్రభుత్వం ముందుకు సాగుతోందని ముఖ్యమంత్రి తెలిపారు. గ్రామాల్లో రైతులకు రైతు భరోసా, మహిళలకు మహాలక్ష్మి పథకం నిరంతరాయంగా అమలవుతున్నాయని వివరించారు. అర్హులైన ప్రతి ఒక్కరికీ ప్రభుత్వ లబ్ధి చేకూరేలా అధికారులు నిరంతరం పర్యవేక్షించాలని ఆదేశించారు. జిల్లా కలెక్టర్లతో నిర్వహించిన ఉన్నత స్థాయి సమీక్షలో సంక్షేమ పథకాల పురోగతిపై ఆయన సంతృప్తి వ్యక్తం చేశారు.",
            imageUrl = "https://images.unsplash.com/photo-1541872703-74c5e44368f9?w=800&auto=format&fit=crop&q=80",
            imageCaption = "హైదరాబాద్ సచివాలయంలో సమీక్షా సమావేశం",
            categoryId = "telangana",
            districtId = "hyderabad",
            authorName = "ఆధ్య ప్రతినిధి",
            status = "published",
            isBreaking = true,
            isFeatured = true,
            viewCount = 1420
        ),
        Article(
            id = "art_2",
            titleTe = "నారాయణపేటలో చేనేత కార్మికులకు ఆధునిక క్లస్టర్ మంజూరు",
            contentTe = "నారాయణపేట: ప్రసిద్ధి చెందిన నారాయణపేట చేనేత చీరల పరిశ్రమను మరింత విస్తరించేందుకు కేంద్ర, రాష్ట్ర ప్రభుత్వాల భాగస్వామ్యంతో మెగా హ్యాండ్లూమ్ క్లస్టర్ ఏర్పాటవుతోంది. దీని ద్వారా వేలాది మంది నేతన్నలకు ఉపాధి మెరుగుపడటంతో పాటు ఆధునిక డిజైన్ శిక్షణ కేంద్రం అందుబాటులోకి వస్తుంది. నేతన్నల ఉత్పత్తులకు ఆన్‌లైన్ గ్లోబల్ మార్కెటింగ్ సదుపాయం కల్పించనున్నారు.",
            imageUrl = "https://images.unsplash.com/photo-1617627143750-d86bc21e42bb?w=800&auto=format&fit=crop&q=80",
            imageCaption = "నారాయణపేట చేనేత మగ్గాలు",
            categoryId = "narayanpet",
            districtId = "narayanpet",
            authorName = "జిల్లా బ్యూరో",
            status = "published",
            isBreaking = false,
            isFeatured = true,
            viewCount = 980
        ),
        Article(
            id = "art_3",
            titleTe = "కొడంగల్ ఎత్తిపోతల పథకం పనుల ముమ్మరం: సాగునీటి రంగంలో విప్లవం",
            contentTe = "కొడంగల్: నియోజకవర్గ పరిధిలోని బీడు భూములకు కృష్ణా జలాలను తరలించే కొడంగల్ - నారాయణపేట ఎత్తిపోతల పథకం పనులు శరవేగంగా కొనసాగుతున్నాయి. ఈ ప్రాజెక్ట్ పూర్తయితే లక్షకు పైగా ఎకరాలకు పుష్కలంగా సాగునీరు అందుతుందని, స్థానిక రైతులకు ఎంతో మేలు జరుగుతుందని ఇరిగేషన్ అధికారులు స్పష్టం చేశారు.",
            imageUrl = "https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=800&auto=format&fit=crop&q=80",
            imageCaption = "ఎత్తిపోతల పథకం నిర్మాణ ప్రాంతం",
            categoryId = "kodangal",
            districtId = "kodangal",
            authorName = "రైతు వార్తా విభాగం",
            status = "published",
            isBreaking = false,
            isFeatured = true,
            viewCount = 760
        ),
        Article(
            id = "art_4",
            titleTe = "మహబూబ్ నగర్ జిల్లాలో ఐటీ టవర్ విస్తరణ: యువతకు భారీగా ఉద్యోగావకాశాలు",
            contentTe = "మహబూబ్ నగర్: పాలమూరు ఐటీ టవర్‌లో మరిన్ని దేశీయ, విదేశీ టెక్ కంపెనీలు తమ కార్యకలాపాలను ప్రారంభించేందుకు సిద్ధమయ్యాయి. ద్వితీయ శ్రేణి నగరాల్లో ఐటీ విస్తరణలో భాగంగా మహబూబ్ నగర్ కీలక కేంద్రంగా మారుతోందని, ఇంజినీరింగ్ గ్రాడ్యుయేట్లకు స్థానికంగానే ఉద్యోగాలు లభిస్తాయని అధికారులు పేర్కొన్నారు.",
            imageUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=800&auto=format&fit=crop&q=80",
            imageCaption = "మహబూబ్ నగర్ ఐటీ హబ్",
            categoryId = "mahabubnagar",
            districtId = "mahabubnagar",
            authorName = "వాణిజ్య డెస్క్",
            status = "published",
            isBreaking = true,
            isFeatured = false,
            viewCount = 1150
        ),
        Article(
            id = "art_5",
            titleTe = "ఇస్రో మరో ఘనత: పీఎస్ఎల్వీ ప్రయోగం విజయవంతం",
            contentTe = "శ్రీహరికోట: భారత అంతరిక్ష పరిశోధన సంస్థ (ఇస్రో) మరో అద్భుత విజయాన్ని నమోదు చేసింది. సతీష్ ధావన్ స్పేస్ సెంటర్ నుంచి ప్రయోగించిన అధునాతన రాకెట్ నిర్దేశిత ఉపగ్రహాన్ని విజయవంతంగా కక్ష్యలోకి ప్రవేశపెట్టింది. శాస్త్రవేత్తల బృందాన్ని ప్రధానమంత్రి అభినందించారు.",
            imageUrl = "https://images.unsplash.com/photo-1517976487507-5b3b4a45091a?w=800&auto=format&fit=crop&q=80",
            imageCaption = "శ్రీహరికోట రాకెట్ ప్రయోగం",
            categoryId = "technology",
            districtId = "",
            authorName = "సైన్స్ కరస్పాండెంట్",
            status = "published",
            isBreaking = false,
            isFeatured = false,
            viewCount = 2300
        ),
        Article(
            id = "art_6",
            titleTe = "వ్యవసాయంలో డ్రోన్ టెక్నాలజీ: రైతులకు సబ్సిడీపై యంత్రాల పంపిణీ",
            contentTe = "హైదరాబాద్: పంటల రక్షణ, ఎరువుల పిచికారీ కోసం డ్రోన్ల వినియోగాన్ని ప్రభుత్వం ప్రోత్సహిస్తోంది. రైతు ఉత్పత్తిదారుల సంఘాలకు భారీ సబ్సిడీతో డ్రోన్ పరికరాలను అందించే కార్యక్రమాన్ని వ్యవసాయ శాఖ మంత్రి ప్రారంభించారు.",
            imageUrl = "https://images.unsplash.com/photo-1508614589041-895b88991e3e?w=800&auto=format&fit=crop&q=80",
            imageCaption = "చేలల్లో వ్యవసాయ డ్రోన్ పిచికారీ",
            categoryId = "agriculture",
            districtId = "",
            authorName = "వ్యవసాయ ప్రతినిధి",
            status = "published",
            isBreaking = false,
            isFeatured = false,
            viewCount = 890
        )
    )

    val sampleVideos = listOf(
        VideoNews(
            id = "vid_1",
            titleTe = "ప్రత్యేక విశ్లేషణ: పాలమూరు - రంగారెడ్డి ప్రాజెక్టు తాజా స్థితిగతులు",
            thumbnailUrl = "https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=800&auto=format&fit=crop&q=80",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            durationSeconds = 180,
            isPublished = true
        ),
        VideoNews(
            id = "vid_2",
            titleTe = "నారాయణపేట చేనేత కళా వైభవం: ప్రత్యేక గ్రౌండ్ రిపోర్ట్",
            thumbnailUrl = "https://images.unsplash.com/photo-1617627143750-d86bc21e42bb?w=800&auto=format&fit=crop&q=80",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            durationSeconds = 240,
            isPublished = true
        ),
        VideoNews(
            id = "vid_3",
            titleTe = "ఆధ్య న్యూస్ బులిటెన్: నేటి ప్రధాన ముఖ్యాంశాలు",
            thumbnailUrl = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=800&auto=format&fit=crop&q=80",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            durationSeconds = 300,
            isPublished = true
        )
    )
}
