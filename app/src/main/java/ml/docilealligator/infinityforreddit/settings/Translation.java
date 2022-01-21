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
        translationContributors.add(new Translation("български", "Ana patriciaaguayogomez, Iliqiliev373, zerw, Кристиян", R.drawable.flag_bulgaria));
        translationContributors.add(new Translation("简体中文", "1, 3273676671, AaronFeng, Angela Thayer, Bitlabwzh, cdggqa, deluxghost, Dwhite, Gloria, gzwoyikythba, History_exe, hyl, Initial_Reading_197, Justin, Ray, Steps, Tunicar, wert, 王昱程", R.drawable.flag_china));
        translationContributors.add(new Translation("繁體中文", "1, Angela Thayer, Hbhuh, Ray", R.drawable.flag_china));
        translationContributors.add(new Translation("Hrvatski", "Andrej Ivanusec, Branimir, Josip Biondić", R.drawable.flag_croatia));
        translationContributors.add(new Translation("čeština", "Fjuro", R.drawable.flag_czech));
        translationContributors.add(new Translation("Nederlands", "Anthony, Heimen Stoffels, KevinHF, Knnf, Khawkfist, Losms67, Mert, Viktor", R.drawable.flag_netherlands));
        translationContributors.add(new Translation("Esperanto", "Ana patriciaaguayogomez, AnimatorzPolski", -1));
        translationContributors.add(new Translation("Française", "367, Clement. wawszczyk, Darkempire78, Darlene Sonalder, escatrag, Finn Olmsted, Furax-31, Imperator, Johan, oursonbleu, Owen, pinembour", R.drawable.flag_france));
        translationContributors.add(new Translation("Deutsche", "adth03, Chris, ducc1, Fornball, Guerda, Hoangseidel02, Jan, Joe, Jorge, Lm41, Manuel, Netto Hikari, Nikodiamond3, Nilsrie1, NotABot34, PhCamp, Tischleindeckdich, translatewingman, translatorwiz, vcdf", R.drawable.flag_germany));
        translationContributors.add(new Translation("Ελληνικά", "fresh, Marios, Viktor", R.drawable.flag_greece));
        translationContributors.add(new Translation("עִברִית", "Ofek Bortz, Yuval", R.drawable.flag_israel));
        translationContributors.add(new Translation("हिंदी", "a, Anonymous, Arya, charu, EnArvy, Harshit S Lawaniya, Mrigendra Bhandari, Nikhilcaddilac, Niranjan, prat, raghav, Roshan, Sachin, Ved", R.drawable.flag_india));
        translationContributors.add(new Translation("Magyar", "Balázs, Bro momento, ekaktusz, Gilgames32, Szmanndani, trebron, Zoltan", R.drawable.flag_hungary));
        translationContributors.add(new Translation("Italiana", "Daniele Basso, DanOlivaw, Enri. braga, Gianni00palmieri, Gillauino, Gio. gavio01, Giovanni, Giovanni Donisi, Lorenzo, Marco, Marco, Matisse, Simoneg. work, ztiaa", R.drawable.flag_italy));
        translationContributors.add(new Translation("日本語", "Hira, Issa, Kerim Demirkaynak, Mrigendra Bhandari, nazo6, Ryan", R.drawable.flag_japan));
        translationContributors.add(new Translation("한국어", "Jcxmt125, Me, noname", R.drawable.flag_south_korea));
        translationContributors.add(new Translation("norsk", "", R.drawable.flag_norway));
        translationContributors.add(new Translation("Polskie", "Adam, bbaster, Chupacabra, Erax, Exp, Kajetan, Maks, quark, ultrakox, xmsc", R.drawable.flag_poland));
        translationContributors.add(new Translation("Português", "., Bruno Guerreiro, Francisco, Gabriel, Henry, Henry, Lucas, Miguel, Ricardo Fontão, Ricky", R.drawable.flag_portugal));
        translationContributors.add(new Translation("Português (BR)", "., Andreaugustoqueiroz999, Asfuri, Davy, Júlia Angst Coelho, João Vieira, John Seila, Kauã Azevedo, Laura Vasconcellos Pereira Felippe, luccipriano, menosmenos, Murilogs7002, Raul S., Ricardo, Ricky, Super_Iguanna, T. tony. br01, vsc", R.drawable.flag_brazil));
        translationContributors.add(new Translation("Română", "Edward, Loading Official, Malinatranslates, RabdăInimăȘiTace", R.drawable.flag_romania));
        translationContributors.add(new Translation("русский язык", "Angela Thayer, Anon, Arseniy Tsekh, aveblazer, CaZzzer, Coolant, Craysy, Draer, elena, flexagoon, Georgiy, Overseen, solokot, Stambro, Tysontl2007, Vova", R.drawable.flag_russia));
        translationContributors.add(new Translation("Soomaali", "Nadir Nour", R.drawable.flag_somalia));
        translationContributors.add(new Translation("Español", "Agustin, Alejandro, Alfredo, Angel, Angela Thayer, Armando, Armando Leyvaleyva, Armando Leyvaleyva, Canutolab, Freddy, Gaynus, Iván Peña, Joel. chrono, Jorge, Luis Antonio, Marcelo, Mario, Meh, Miguel, mvstermoe, Nana Snixx, Sergio, Sofia Flores, Suol, Theofficialdork, Tirso Carranza", R.drawable.flag_spain));
        translationContributors.add(new Translation("svenska", "Marcus Nordberg", R.drawable.flag_sweden));
        translationContributors.add(new Translation("தமிழ்", "Gobinathal8", -1));
        translationContributors.add(new Translation("Türkçe", "adth03, Berk Bakır \"Faoiltiarna\", cevirgen, Emir481, Faoiltiarna, Mehmet Yavuz, Mert, Serif", R.drawable.flag_turkey));
        translationContributors.add(new Translation("Українська", "@andmizyk, Andrij Mizyk", R.drawable.flag_ukraine));
        translationContributors.add(new Translation("Tiếng Việt", "bruh, fanta, Kai, Khai, Laezzy, Lmao, Opstober, Ryan, viecdet69", R.drawable.flag_vietnam));
        return translationContributors;
    }
}
