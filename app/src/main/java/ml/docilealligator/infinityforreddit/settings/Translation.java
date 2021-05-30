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
        translationContributors.add(new Translation("български", "Iliqiliev373, zerw, Кристиян", R.drawable.flag_bulgaria));
        translationContributors.add(new Translation("简体中文", "1, 3273676671, AaronFeng, Angela Thayer, Bitlabwzh, cdggqa, deluxghost, Dwhite, Gloria, gzwoyikythba, History_exe, Justin, Ray, Steps, Tunicar, wert", R.drawable.flag_china));
        translationContributors.add(new Translation("繁體中文", "1, Angela Thayer, Hbhuh, Ray", R.drawable.flag_china));
        translationContributors.add(new Translation("Hrvatski", "Andrej Ivanusec, Josip", R.drawable.flag_croatia));
        translationContributors.add(new Translation("Nederlands", "KevinHF, Khawkfist, Mert", R.drawable.flag_netherlands));
        translationContributors.add(new Translation("Esperanto", "AnimatorzPolski", -1));
        translationContributors.add(new Translation("Française", "367, Darlene Sonalder, escatrag, Finn Olmsted, Imperator, Johan, Kerim Demirkaynak, oursonbleu, Owen, pinembour", R.drawable.flag_france));
        translationContributors.add(new Translation("Deutsche", "adth03, ducc1, Jan, Joe, Jorge, Kerim Demirkaynak, Lm41, Netto Hikari, Nikodiamond3, NotABot34, Tischleindeckdich, translatewingman, translatorwiz", R.drawable.flag_germany));
        translationContributors.add(new Translation("Ελληνικά", "fresh, Marios, Viktor", R.drawable.flag_greece));
        translationContributors.add(new Translation("עִברִית", "Yuval", R.drawable.flag_israel));
        translationContributors.add(new Translation("हिंदी", "a, Arya, charu, EnArvy, Harshit S Lawaniya, Mrigendra Bhandari, Nikhilcaddilac, prat, raghav, Sachin, Ved", R.drawable.flag_india));
        translationContributors.add(new Translation("Magyar", "Balázs, Bro momento, ekaktusz, trebron, Zoltan", R.drawable.flag_hungary));
        translationContributors.add(new Translation("Italiana", "Daniele Basso, DanOlivaw, Enri. braga, Gianni00palmieri, Gillauino, Gio. gavio01, Giovanni, Giovanni Donisi, Lorenzo, Matisse, Simoneg. work, ztiaa", R.drawable.flag_italy));
        translationContributors.add(new Translation("日本語", "Hira, Issa, Kerim Demirkaynak, Mrigendra Bhandari, Ryan", R.drawable.flag_japan));
        translationContributors.add(new Translation("한국어", "noname", R.drawable.flag_south_korea));
        translationContributors.add(new Translation("norsk", "", R.drawable.flag_norway));
        translationContributors.add(new Translation("Polskie", "Chupacabra, Erax, Kajetan, Maks, quark, ultrakox", R.drawable.flag_poland));
        translationContributors.add(new Translation("Português", "., Bruno Guerreiro, Gabriel, Henry, Lucas, Ricky", R.drawable.flag_portugal));
        translationContributors.add(new Translation("Português (BR)", "., Asfuri, Davy, Júlia Angst Coelho, John Seila, Laura Vasconcellos Pereira Felippe, luccipriano, Raul S., Ricardo, Ricky, Super_Iguanna, T. tony. br01, vsc", R.drawable.flag_brazil));
        translationContributors.add(new Translation("Română", "Loading Official, RabdăInimăȘiTace", R.drawable.flag_romania));
        translationContributors.add(new Translation("русский язык", "Angela Thayer, Anon, Arseniy Tsekh, aveblazer, CaZzzer, Coolant, Craysy, Draer, elena, flexagoon, Georgiy, Overseen, Stambro, Tysontl2007, Vova", R.drawable.flag_russia));
        translationContributors.add(new Translation("Soomaali", "Nadir Nour", R.drawable.flag_somalia));
        translationContributors.add(new Translation("Español", "Agustin, Alejandro, Alfredo, Angel, Angela Thayer, Canutolab, Gaynus, Jorge, Luis Antonio, Meh, Miguel, mvstermoe, Nana Snixx, Sergio, Sofia Flores, Theofficialdork, Tirso Carranza", R.drawable.flag_spain));
        translationContributors.add(new Translation("svenska", "Marcus Nordberg", R.drawable.flag_sweden));
        translationContributors.add(new Translation("Türkçe", "adth03, Berk Bakır \"Faoiltiarna\", Emir481, Faoiltiarna, Kerim Demirkaynak, Mehmet Yavuz, Mert, Serif", R.drawable.flag_turkey));
        translationContributors.add(new Translation("Tiếng Việt", "bruh, Kai, Laezzy, Lmao, Ryan, viecdet69", R.drawable.flag_vietnam));
        return translationContributors;
    }
}
