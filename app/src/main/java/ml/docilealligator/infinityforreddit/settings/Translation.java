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
        translationContributors.add(new Translation("български", "Ana patriciaaguayogomez, Iliqiliev373, Nane, zerw, Кристиян", R.drawable.flag_bulgaria));
        translationContributors.add(new Translation("简体中文", "1, 3273676671, AaronFeng, Angela Thayer, Bitlabwzh, cdggqa, deluxghost, Dwhite, Gloria, gzwoyikythba, History_exe, hyl, Initial_Reading_197, Justin, Kai yuan, Ray, Steps, Tunicar, wert, WhiCCX5, 王昱程", R.drawable.flag_china));
        translationContributors.add(new Translation("繁體中文", "1, Angela Thayer, Hbhuh, Ray, shlp, Wolfy. coding", R.drawable.flag_china));
        translationContributors.add(new Translation("Hrvatski", "Andrej Ivanusec, Branimir, Josip Biondić", R.drawable.flag_croatia));
        translationContributors.add(new Translation("čeština", "Fjuro, Jeniktelefon, sidvic88", R.drawable.flag_czech));
        translationContributors.add(new Translation("Nederlands", "a, Anthony, Heimen Stoffels, KevinHF, Knnf, Khawkfist, Losms67, Mert, Viktor", R.drawable.flag_netherlands));
        translationContributors.add(new Translation("Esperanto", "Ana patriciaaguayogomez, AnimatorzPolski, LiftedStarfish", -1));
        translationContributors.add(new Translation("Française", "367, Charlito33, Clement. wawszczyk, Darkempire78, Darlene Sonalder, escatrag, Finn Olmsted, Furax-31, Hypnoticbat9555, Imperator, Johan, Loïc, Me1s, oursonbleu, Owen, pinembour, Serviceclient3dmart, Thomas", R.drawable.flag_france));
        translationContributors.add(new Translation("Deutsche", "adth03, Chris, ducc1, Fornball, Guerda, Hoangseidel02, James, Jan, Joe, Jorge, Justus, Lm41, Manuel, Maximilian. neumann2, Netto Hikari, Nilsrie1, Nikodiamond3, Nilsrie1, NotABot34, PhCamp, Splat, Tischleindeckdich, translatewingman, translatorwiz, vcdf", R.drawable.flag_germany));
        translationContributors.add(new Translation("Ελληνικά", "fresh, Marios, Viktor, Winston", R.drawable.flag_greece));
        translationContributors.add(new Translation("עִברִית", "Ofek Bortz, Yuval", R.drawable.flag_israel));
        translationContributors.add(new Translation("हिंदी", "a, Anonymous, Arya, charu, EnArvy, Harshit S Lawaniya, Mrigendra Bhandari, Nikhilcaddilac, Niranjan, prat, raghav, raj, Roshan, Sachin, saqib, Ved", R.drawable.flag_india));
        translationContributors.add(new Translation("Magyar", "Balázs, Bro momento, ekaktusz, Gilgames32, mdvhimself, Szmanndani, trebron, Zoltan", R.drawable.flag_hungary));
        translationContributors.add(new Translation("Italiana", "Daniele Basso, DanOlivaw, Enri. braga, Gianni00palmieri, Gillauino, Gio. gavio01, Giovanni, Giovanni Donisi, Lorenzo, Marco, Marco, Matisse, Simoneg. work, ztiaa", R.drawable.flag_italy));
        translationContributors.add(new Translation("日本語", "Hira, Issa, Mrigendra Bhandari, nazo6, Ryan", R.drawable.flag_japan));
        translationContributors.add(new Translation("한국어", "Jcxmt125, Me, noname", R.drawable.flag_south_korea));
        translationContributors.add(new Translation("norsk", "", R.drawable.flag_norway));
        translationContributors.add(new Translation("Polskie", "Adam, bbaster, Chupacabra, crash, Erax, Exp, Indexerrowaty, Kajetan, Maks, needless, quark, ultrakox, XioR112, xmsc", R.drawable.flag_poland));
        translationContributors.add(new Translation("Português", "., Bruno Guerreiro, Francisco, Gabriel, Henry, Henry, Lucas, Miguel, Ricardo Fontão, Ricky", R.drawable.flag_portugal));
        translationContributors.add(new Translation("Português (BR)", "., Andreaugustoqueiroz999, Asfuri, Davy, Júlia Angst Coelho, João Vieira, John Seila, Kauã Azevedo, Laura Vasconcellos Pereira Felippe, luccipriano, menosmenos, Murilogs7002, Raul S., Ricardo, Ricky, Sousa, Super_Iguanna, T. tony. br01, vsc, Ryan Marcelo", R.drawable.flag_brazil));
        translationContributors.add(new Translation("Română", "Arminandrey, BitterJames, Cosmin, Edward, Loading Official, Malinatranslates, RabdăInimăȘiTace", R.drawable.flag_romania));
        translationContributors.add(new Translation("русский язык", "Angela Thayer, Anon, Arseniy Tsekh, aveblazer, CaZzzer, Coolant, Craysy, Draer, elena, flexagoon, Georgiy, InvisibleRain, Overseen, solokot, Stambro, Tysontl2007, Vova", R.drawable.flag_russia));
        translationContributors.add(new Translation("Soomaali", "Nadir Nour", R.drawable.flag_somalia));
        translationContributors.add(new Translation("Español", "Agustin, Alejandro, Alfredo, Alonso, Angel, Angela Thayer, Armando, Armando Leyvaleyva, Armando Leyvaleyva, Canutolab, Freddy, Galdric, Gaynus, Iván Peña, Joel. chrono, Jorge, Kai yuan, Luis Antonio, Marcelo, Mario, Meh, Miguel, mvstermoe, Nana Snixx, Sergio, Sergio Varela, Sofia Flores, Suol, Theofficialdork, Tirso Carranza", R.drawable.flag_spain));
        translationContributors.add(new Translation("svenska", "Marcus Nordberg", R.drawable.flag_sweden));
        translationContributors.add(new Translation("தமிழ்", "Gobinathal8", -1));
        translationContributors.add(new Translation("Türkçe", "adth03, Bahasnyldz, Berk Bakır \"Faoiltiarna\", cevirgen, Emir481, Kerim, Faoiltiarna, Mehmet Yavuz, Mert, Serif, Tuna Mert", R.drawable.flag_turkey));
        translationContributors.add(new Translation("Українська", "@andmizyk, Andrij Mizyk", R.drawable.flag_ukraine));
        translationContributors.add(new Translation("Tiếng Việt", "bruh, Đỗ Quang Vinh, fanta, harrybruh-kun, Kai, Khai, Laezzy, Lmao, Opstober, Ryan, viecdet69", R.drawable.flag_vietnam));
        return translationContributors;
    }
}
