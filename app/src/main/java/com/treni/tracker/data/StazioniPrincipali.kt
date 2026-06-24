package com.treni.tracker.data

/**
 * Elenco delle principali stazioni italiane con coordinate geografiche,
 * usato per calcolare le stazioni più vicine alla posizione dell'utente.
 * Non è un elenco completo di tutte le stazioni (sarebbero migliaia),
 * ma copre i principali capoluoghi e nodi ferroviari, sufficiente per
 * un suggerimento utile nella maggior parte dei casi.
 */
data class StazionePrincipale(
    val nome: String,
    val lat: Double,
    val lon: Double
)

val STAZIONI_PRINCIPALI = listOf(
    StazionePrincipale("MILANO CENTRALE", 45.4862, 9.2046),
    StazionePrincipale("MILANO PORTA GARIBALDI", 45.4842, 9.1880),
    StazionePrincipale("ROMA TERMINI", 41.9009, 12.5018),
    StazionePrincipale("ROMA TIBURTINA", 41.9123, 12.5311),
    StazionePrincipale("TORINO PORTA NUOVA", 45.0628, 7.6790),
    StazionePrincipale("NAPOLI CENTRALE", 40.8527, 14.2730),
    StazionePrincipale("BOLOGNA CENTRALE", 44.5054, 11.3430),
    StazionePrincipale("FIRENZE SANTA MARIA NOVELLA", 43.7766, 11.2480),
    StazionePrincipale("VENEZIA SANTA LUCIA", 45.4413, 12.3204),
    StazionePrincipale("GENOVA PIAZZA PRINCIPE", 44.4138, 8.9133),
    StazionePrincipale("BARI CENTRALE", 41.1175, 16.8719),
    StazionePrincipale("PALERMO CENTRALE", 38.1108, 13.3608),
    StazionePrincipale("VERONA PORTA NUOVA", 45.4292, 10.9837),
    StazionePrincipale("PADOVA", 45.4146, 11.8807),
    StazionePrincipale("BRESCIA", 45.5251, 10.2222),
    StazionePrincipale("BOLZANO", 46.4943, 11.3548),
    StazionePrincipale("TRIESTE CENTRALE", 45.6500, 13.7826),
    StazionePrincipale("PERUGIA", 43.1145, 12.4022),
    StazionePrincipale("ANCONA", 43.6042, 13.5128),
    StazionePrincipale("PESCARA CENTRALE", 42.4646, 14.2161),
    StazionePrincipale("REGGIO EMILIA AV MEDIOPADANA", 44.6975, 10.6526),
    StazionePrincipale("PARMA", 44.8132, 10.3354),
    StazionePrincipale("PIACENZA", 45.0526, 9.6986),
    StazionePrincipale("MODENA", 44.6471, 10.9252),
    StazionePrincipale("CATANIA CENTRALE", 37.4870, 15.0865),
    StazionePrincipale("MESSINA CENTRALE", 38.1847, 15.5526),
    StazionePrincipale("REGGIO CALABRIA CENTRALE", 38.1098, 15.6515),
    StazionePrincipale("SALERNO", 40.6700, 14.7886),
    StazionePrincipale("CAGLIARI", 39.2178, 9.1135),
    StazionePrincipale("LECCE", 40.3522, 18.1741),
    StazionePrincipale("FOGGIA", 41.4595, 15.5485),
    StazionePrincipale("UDINE", 46.0639, 13.2358),
    StazionePrincipale("LA SPEZIA CENTRALE", 44.1069, 9.8267),
    StazionePrincipale("PISA CENTRALE", 43.7090, 10.4015),
    StazionePrincipale("LIVORNO CENTRALE", 43.5474, 10.3170),
    StazionePrincipale("AREZZO", 43.4633, 11.8807),
    StazionePrincipale("SIENA", 43.3192, 11.3308),
    StazionePrincipale("COMO SAN GIOVANNI", 45.8081, 9.0759),
    StazionePrincipale("VARESE", 45.8159, 8.8281),
    StazionePrincipale("MONZA", 45.5829, 9.2737),
    StazionePrincipale("BERGAMO", 45.6928, 9.6699),
    StazionePrincipale("MANTOVA", 45.1539, 10.7935),
    StazionePrincipale("CREMONA", 45.1335, 10.0282),
    StazionePrincipale("FERRARA", 44.8350, 11.6177),
    StazionePrincipale("RIMINI", 44.0617, 12.5750),
    StazionePrincipale("RAVENNA", 44.4140, 12.2027)
)

/**
 * Calcola la distanza approssimata in km tra due coordinate (formula
 * di Haversine semplificata, sufficiente per ordinare per vicinanza).
 */
fun distanzaKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
        Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return r * c
}

/** Restituisce le n stazioni principali più vicine a una posizione data. */
fun stazioniPiuVicine(lat: Double, lon: Double, n: Int = 4): List<String> {
    return STAZIONI_PRINCIPALI
        .sortedBy { distanzaKm(lat, lon, it.lat, it.lon) }
        .take(n)
        .map { it.nome }
}
