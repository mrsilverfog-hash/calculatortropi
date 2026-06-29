private static void dessiner(DrawContext contexte, RenderTickCounter compteur) {
    MinecraftClient client = MinecraftClient.getInstance();
    if (client.player == null) return;

    ClientBattle combat = CobblemonClient.INSTANCE.getBattle();
    if (combat == null) return;

    // On identifie "mon" Pokémon actif et celui d'en face en comparant
    // l'UUID du Pokémon lui-même à ceux de ma party, plutôt que l'UUID de
    // l'acteur au joueur connecté. Plus robuste face à des systèmes comme
    // un terminal de combat classé, qui peuvent envelopper les acteurs
    // différemment d'un duel classique.
    java.util.Set<UUID> uuidsDeMaParty = new java.util.HashSet<>();
    for (Pokemon p : CobblemonClient.INSTANCE.getStorage().getParty()) {
        if (p != null) uuidsDeMaParty.add(p.getUuid());
    }

    ClientBattlePokemon monActif = null;
    ClientBattlePokemon actifAdverse = null;

    for (ClientBattleSide cote : combat.getSides()) {
        for (ClientBattleActor acteur : cote.getActors()) {
            for (ActiveClientBattlePokemon actif : acteur.getActivePokemon()) {
                ClientBattlePokemon bp = actif.getBattlePokemon();
                if (bp == null) continue;
                if (uuidsDeMaParty.contains(bp.getUuid())) {
                    monActif = bp;
                } else if (actifAdverse == null) {
                    actifAdverse = bp;
                }
            }
        }
    }

    if (monActif == null || actifAdverse == null) return;

    Pokemon monPokemonComplet = null;
    for (Pokemon p : CobblemonClient.INSTANCE.getStorage().getParty()) {
        if (p != null && p.getUuid().equals(monActif.getUuid())) {
            monPokemonComplet = p;
            break;
        }
    }
    if (monPokemonComplet == null) return;
