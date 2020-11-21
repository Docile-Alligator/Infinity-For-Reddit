package ml.docilealligator.infinityforreddit.settings;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.R;

public class Translation {
    public String language;
    public String contributors;
    public int flagDrawableId;

    public Translation(String language, String contributors, int flagDrawableId) {
        this.language = language;
        this.contributors = contributors;
        this.flagDrawableId = flagDrawableId;
    }

    public static ArrayList<Translation> getTranslationContributors() {
        ArrayList<Translation> translationContributors = new ArrayList<>();
        translationContributors.add(new Translation("български", "zerw, Кристиян", R.drawable.flag_bulgaria));
        translationContributors.add(new Translation("简体中文", "Angela Thayer, cdggqa, Gloria, Justin, Ray, Steps", R.drawable.flag_china));
        translationContributors.add(new Translation("繁體中文", "Angela Thayer, Ray", R.drawable.flag_china));
        translationContributors.add(new Translation("Hrvatski", "Josip", R.drawable.flag_croatia));
        translationContributors.add(new Translation("Nederlands", "Khawkfist, Mert", R.drawable.flag_netherlands));
        translationContributors.add(new Translation("Française", "367, Darlene Sonalder, Finn Olmsted, Imperator, oursonbleu, Owen, pinembour", R.drawable.flag_france));
        translationContributors.add(new Translation("Deutsche", "Jorge, Netto Hikari, Nikodiamond3, translatewingman, translatorwiz", R.drawable.flag_germany));
        translationContributors.add(new Translation("Ελληνικά", "Marios, Viktor", R.drawable.flag_greece));
        translationContributors.add(new Translation("हिंदी", "a, Arya, charu, Mrigendra Bhandari, prat, raghav, Sachin, Ved", R.drawable.flag_india));
        translationContributors.add(new Translation("Magyar", "Zoltan", R.drawable.flag_hungary));
        translationContributors.add(new Translation("Italiana", "Daniele Basso, DanOlivaw, Gianni00palmieri, Gillauino, Matisse, Simoneg. work, ztiaa", R.drawable.flag_italy));
        translationContributors.add(new Translation("日本語", "Hira, Issa, Mrigendra Bhandari, Ryan", R.drawable.flag_japan));
        translationContributors.add(new Translation("한국어", "noname", R.drawable.flag_south_korea));
        translationContributors.add(new Translation("norsk", "", R.drawable.flag_norway));
        translationContributors.add(new Translation("Polskie", "Chupacabra, Maks", R.drawable.flag_poland));
        translationContributors.add(new Translation("Português", "Bruno Guerreiro, Lucas, Ricky", R.drawable.flag_portugal));
        translationContributors.add(new Translation("Português (BR)", "., Laura Vasconcellos Pereira Felippe, Ricardo, Ricky, Super_Iguanna", R.drawable.flag_brazil));
        translationContributors.add(new Translation("Română", "Loading Official", R.drawable.flag_romania));
        translationContributors.add(new Translation("русский язык", "Angela Thayer, aveblazer, flexagoon, Georgiy, Vova", R.drawable.flag_russia));
        translationContributors.add(new Translation("Español", "Alejandro, Angela Thayer, Gaynus, Jorge, Miguel, mvstermoe, Nana Snixx, Sofia Flores", R.drawable.flag_spain));
        translationContributors.add(new Translation("svenska", "Marcus Nordberg", R.drawable.flag_sweden));
        translationContributors.add(new Translation("Türkçe", "Emir481, Faoiltiarna, Kerim Demirkaynak, Mehmet Yavuz, Serif", R.drawable.flag_turkey));
        translationContributors.add(new Translation("Tiếng Việt", "Kai, Laezzy, Lmao, Ryan, viecdet69", R.drawable.flag_vietnam));
        return translationContributors;
    }
}
