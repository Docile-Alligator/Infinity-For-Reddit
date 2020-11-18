package ml.docilealligator.infinityforreddit.award;

import java.util.ArrayList;

public class Award {
    private String id;
    private String awardSubType;
    private String name;
    private String description;
    private String iconUrl;
    private int coinPrice;

    public Award(String id, String awardSubType, String name, String description, String iconUrl, int coinPrice) {
        this.id = id;
        this.awardSubType = awardSubType;
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
        this.coinPrice = coinPrice;
    }

    public static ArrayList<Award> getAvailableAwards() {
        ArrayList<Award> awards = new ArrayList<>();
        awards.add(new Award("gid_2", "GLOBAL", "Gold", "Gives the author a week of Reddit Premium, 100 Coins to do with as they please, and shows a Gold Award.", "https://www.redditstatic.com/gold/awards/icon/gold_512.png", 500));
        awards.add(new Award("gid_3", "GLOBAL", "Platinum", "Gives the author a month of Reddit Premium, which includes 700 Coins for that month, and shows a Platinum Award.", "https://www.redditstatic.com/gold/awards/icon/platinum_512.png", 1800));
        awards.add(new Award("award_4ca5a4e6-8873-4ac5-99b9-71b1d5161a91", "GLOBAL", "Argentium", "Latin for distinguished. Shimmers like silver & stronger than steel. When someone deserves outsize recognition. This award gives a three-month Premium subscription and 2500 coins to the recipient.", "https://www.redditstatic.com/gold/awards/icon/Mithril_512.png", 20000));
        awards.add(new Award("award_2385c499-a1fb-44ec-b9b7-d260f3dc55de", "GLOBAL", "Ternion All-Powerful", "Legendary level. A no holds barred celebration of something that hits you in the heart, mind and soul. Some might call it unachievanium. Gives the author 6 months of Premium and 5000 Coins.", "https://www.redditstatic.com/gold/awards/icon/Trinity_512.png", 50000));
        awards.add(new Award("gid_1", "GLOBAL", "Silver", "Shows the Silver Award... and that's it.", "https://www.redditstatic.com/gold/awards/icon/silver_512.png", 100));
        awards.add(new Award("award_e55d1889-11f2-4d04-8abb-44b1de7dd53d", "GLOBAL", "Aww-some", "Use the Aww-some Award to highlight comments that are absolutely adorable.", "https://www.redditstatic.com/gold/awards/icon/Awwsome_512.png", 350));
        awards.add(new Award("award_11eb6af3-3d0d-4d70-8261-22d216ab591d", "GLOBAL", "Heartbeat", "Use the Heartbeat Award to highlight comments that make you feel warm and fuzzy", "https://www.redditstatic.com/gold/awards/icon/Heartbeat_512.png", 350));
        awards.add(new Award("award_0e957fb0-c8f1-4ba1-a8ef-e1e524b60d7d", "GLOBAL", "Starry", "Use the Starry Award to highlight comments that deserve to stand out from the crowd.", "https://www.redditstatic.com/gold/awards/icon/Starry_512.png", 500));
        awards.add(new Award("award_75f9bc56-eba3-4988-a1af-aec974404a0b", "GROUP", "Super Medal Train", "All aboard! Every 5 Awards gives the author 100 Coins and 1 week of Premium. Rack up the number of Awards and watch the Train level-up.", "https://i.redd.it/award_images/t5_22cerq/v3wyujfap4p51_SuperMedalTrain.png", 125));
        awards.add(new Award("award_7149a401-1223-4543-bfd6-4127cb4246da", "GLOBAL", "Cool Summer", "The key is to keep your cool this summer.", "https://i.redd.it/award_images/t5_22cerq/1m9abin81nc51_CoolSummer.png", 50));
        awards.add(new Award("award_02d9ab2c-162e-4c01-8438-317a016ed3d9", "GLOBAL", "Take My Energy", "I'm in this with you.", "https://i.redd.it/award_images/t5_22cerq/898sygoknoo41_TakeMyEnergy.png", 50));
        awards.add(new Award("award_7becef23-fb0b-4d62-b8a6-01d5759367cb", "GLOBAL", "Faith In Humanity Restored", "When goodness lifts you", "https://i.redd.it/award_images/t5_22cerq/gva4vt20qc751_FaithInHumanityRestored.png", 70));
        awards.add(new Award("award_84276b1e-cc8f-484f-a19c-be6c09adc1a5", "GLOBAL", "Bravo!", "An amazing showing.", "https://www.redditstatic.com/gold/awards/icon/SnooClapping_512.png", 400));
        awards.add(new Award("award_68ba1ee3-9baf-4252-be52-b808c1e8bdc4", "GLOBAL", "This", "Me approved", "https://i.redd.it/award_images/t5_22cerq/vu6om0xnb7e41_This.png", 300));
        awards.add(new Award("award_1f0462ee-18f5-4f33-89cf-f1f79336a452", "GLOBAL", "Wholesome (Pro)", "When you come across a feel-good thing. Gives 100 Coins to both the author and the community.", "https://i.redd.it/award_images/t5_22cerq/0o2j782f00e41_WholesomeSuperpro.png", 500));
        awards.add(new Award("award_3dd248bc-3438-4c5b-98d4-24421fd6d670", "GLOBAL", "Coin Gift", "Give the gift of 250 Reddit Coins.", "https://i.redd.it/award_images/t5_22cerq/cr1mq4yysv541_CoinGift.png", 300));
        awards.add(new Award("award_2ae56630-cfe0-424e-b810-4945b9145358", "GLOBAL", "Helpful (Pro)", "Thank you stranger. Gives 100 Coins to both the author and the community.", "https://i.redd.it/award_images/t5_22cerq/trz28na8ajz31_Helpful.png", 500));
        awards.add(new Award("award_6220ecfe-4552-4949-aa13-fb1fb7db537c", "GLOBAL", "Super Heart Eyes", "When the love is out of control.", "https://www.redditstatic.com/gold/awards/icon/Superheart_512.png", 325));
        awards.add(new Award("award_5b39e8fd-7a58-4cbe-8ca0-bdedd5ed1f5a", "GLOBAL", "Doot 🎵 Doot", "Sometimes you just got to dance with the doots.", "https://www.redditstatic.com/gold/awards/icon/Updoot_512.png", 400));
        awards.add(new Award("award_9f928aff-c9f5-4e7e-aa91-8619dce60f1c", "GLOBAL", "Table Slap", "When laughter meets percussion", "https://www.redditstatic.com/gold/awards/icon/TableSlap_512.png", 325));
        awards.add(new Award("award_3409a4c0-ba69-43a0-be9f-27bc27c159cc", "GLOBAL", "Spit-take", "Shower them with laughs", "https://www.redditstatic.com/gold/awards/icon/Spits_drink_512.png", 325));
        awards.add(new Award("award_3267ca1c-127a-49e9-9a3d-4ba96224af18", "GLOBAL", "I'll Drink to That", "Let's sip to good health and good company", "https://i.redd.it/award_images/t5_22cerq/45aeu8mzvsj51_IllDrinktoThat.png", 100));
        awards.add(new Award("award_31260000-2f4a-4b40-ad20-f5aa46a577bf", "APPRECIATION", "Timeless Beauty", "Beauty that's forever. Gives 100 Coins each to the author and the community.", "https://i.redd.it/award_images/t5_22cerq/crhlsu5wzlc41_TimelessBeauty.png", 250));
        awards.add(new Award("award_69c94eb4-d6a3-48e7-9cf2-0f39fed8b87c", "GLOBAL", "Ally", "Listen, get educated, and get involved.", "https://i.redd.it/award_images/t5_22cerq/5nswjpyy44551_Ally.png", 50));
        awards.add(new Award("award_a903c949-ccc5-420d-8239-1bbefc424838", "GLOBAL", "Healthcare Hero", "Putting yourself on the line for us - you are the perfect super hero!", "https://i.redd.it/award_images/t5_22cerq/xs2na1t1v9p41_HealthcareHero.png", 50));
        awards.add(new Award("award_03c4f93d-efc7-463b-98a7-c01814462ab0", "GLOBAL", "I am disappoint", "I'm not mad, I'm just disappointed.", "https://i.redd.it/award_images/t5_22cerq/3ekkailk5s551_Iamdisappoint.png", 50));
        awards.add(new Award("award_d33fddd7-a58a-4472-b1a2-3157d8c8b76f", "GLOBAL", "Looking Busy", "Looking like you're working is hard work.", "https://i.redd.it/award_images/t5_22cerq/k0qzautvyyk51_LookingBusy.png", 50));
        awards.add(new Award("award_e1b2bf9e-8c62-4edc-9b6d-ffa44a7cb53b", "GLOBAL", "Recharge", "Feeling relaxed and restored", "https://i.redd.it/award_images/t5_22cerq/0lurnbnhqkl51_Recharge.png", 50));
        awards.add(new Award("award_80d4d339-95d0-43ac-b051-bc3fe0a9bab8", "GLOBAL", "Wearing is Caring", "Keep the community and yourself healthy and happy.", "https://i.redd.it/award_images/t5_22cerq/lcswc5d07hb51_WearingisCaring.png", 50));
        awards.add(new Award("award_b1b44fa1-8179-4d84-a9ed-f25bb81f1c5f", "GLOBAL", "Facepalm", "*Lowers face into palm*", "https://i.redd.it/award_images/t5_22cerq/ey2iodron2s41_Facepalm.png", 70));
        awards.add(new Award("award_99d95969-6100-45b2-b00c-0ec45ae19596", "GLOBAL", "Snek", "A smol, delicate danger noodle.", "https://i.redd.it/award_images/t5_22cerq/rc5iesz2z8t41_Snek.png", 70));
        awards.add(new Award("award_b92370bb-b7de-4fb3-9608-c5b4a22f714a", "GLOBAL", "Tree Hug", "Show nature some love.", "https://i.redd.it/award_images/t5_22cerq/fukjtec638u41_TreeHug.png", 70));
        awards.add(new Award("award_ae89e420-c4a5-47b8-a007-5dacf1c0f0d4", "GLOBAL", "Lawyer Up", "OBJECTION!", "https://i.redd.it/award_images/t5_22cerq/iq0sgwn5bzy41_LawyerUp.png", 75));
        awards.add(new Award("award_8352bdff-3e03-4189-8a08-82501dd8f835", "GLOBAL", "Hugz", "Everything is better with a good hug", "https://i.redd.it/award_images/t5_22cerq/niiatoknifn51_Hugz.png", 80));
        awards.add(new Award("award_869d4135-8738-41e5-8630-de593b4f049f", "GLOBAL", "'MURICA", "Did somebody say 'Murica?", "https://i.redd.it/award_images/t5_22cerq/18mwqw5th9e51_MURICA.png", 100));
        awards.add(new Award("award_81cf5c92-8500-498c-9c94-3e4034cece0a", "GLOBAL", "Dread", "Staring into the abyss and it's staring right back", "https://i.redd.it/award_images/t5_22cerq/nvfe4gyawnf51_Dread.png", 100));
        awards.add(new Award("award_483d8e29-bbe5-404e-a09a-c2d7b16c4fff", "GLOBAL", "Evil Cackle", "Laugh like a supervillain", "https://i.redd.it/award_images/t5_22cerq/43zl6dfcg9e51_EvilCackle.png", 100));
        awards.add(new Award("award_74fe5152-7906-4991-9016-bc2d8e261200", "GLOBAL", "Excited", "I don't know what to do with my hands!", "https://i.redd.it/award_images/t5_22cerq/x069ow7ewnf51_Excited.png", 100));
        awards.add(new Award("award_01178870-6a4f-4172-8f36-9ed5092ee4f9", "GLOBAL", "Glow Up", "You look amazing, glowing, incredible!", "https://i.redd.it/award_images/t5_22cerq/2754pa5jvsj51_GlowUp.png", 100));
        awards.add(new Award("award_19860e30-3331-4bac-b3d1-bd28de0c7974", "GLOBAL", "Heartwarming", "I needed this today", "https://i.redd.it/award_images/t5_22cerq/v1mxw8i6wnf51_Heartwarming.png", 100));
        awards.add(new Award("award_1da6ff27-7c0d-4524-9954-86e5cda5fcac", "GLOBAL", "Keep Calm", "Stop, chill, relax", "https://i.redd.it/award_images/t5_22cerq/g77c4oud7hb51_KeepCalm.png", 100));
        awards.add(new Award("award_1e516e18-cbee-4668-b338-32d5530f91fe", "GLOBAL", "Kiss", "You deserve a smooch", "https://i.redd.it/award_images/t5_22cerq/sb42u5gmwsj51_Kiss.png", 100));
        awards.add(new Award("award_b4072731-c0fb-4440-adc7-1063d6a5e6a0", "GLOBAL", "Masterpiece", "C'est magnifique", "https://i.redd.it/award_images/t5_22cerq/2juh333m40n51_Masterpiece.png", 100));
        awards.add(new Award("award_fbe9527a-adb3-430e-af1a-5fd3489e641b", "GLOBAL", "Shocked", "I'm genuinely flabbergasted.", "https://i.redd.it/award_images/t5_22cerq/fck3iedi2ug51_Shocked.png", 100));
        awards.add(new Award("award_0b41ba9b-8ad9-42c8-85b1-942d5462c830", "GLOBAL", "Starts Recording", "This should be good", "https://i.redd.it/award_images/t5_22cerq/zlrdwu9kh9e51_StartsRecording.png", 100));
        awards.add(new Award("award_43f3bf99-92d6-47ab-8205-130d26e7929f", "GLOBAL", "Tearing Up", "This hits me right in the feels", "https://i.redd.it/award_images/t5_22cerq/lop66ut2wnf51_TearingUp.png", 100));
        awards.add(new Award("award_ae7f17fb-6538-4c75-9ff4-5f48b4cdaa94", "GLOBAL", "Yummy", "That looks so good", "https://i.redd.it/award_images/t5_22cerq/a7dhg27hvnf51_Yummy.png", 100));
        awards.add(new Award("award_5f123e3d-4f48-42f4-9c11-e98b566d5897", "GLOBAL", "Wholesome", "When you come across a feel-good thing.", "https://i.redd.it/award_images/t5_22cerq/5izbv4fn0md41_Wholesome.png", 125));
        awards.add(new Award("award_77ba55a2-c33c-4351-ac49-807455a80148", "GLOBAL", "Bless Up", "Prayers up for the blessed.", "https://i.redd.it/award_images/t5_22cerq/trfv6ems1md41_BlessUp.png", 150));
        awards.add(new Award("award_c42dc561-0b41-40b6-a23d-ef7e110e739e", "GLOBAL", "Buff Doge", "So buff, wow", "https://i.redd.it/award_images/t5_22cerq/zc4a9vk5zmc51_BuffDoge.png", 150));
        awards.add(new Award("award_f44611f1-b89e-46dc-97fe-892280b13b82", "GLOBAL", "Helpful", "Thank you stranger. Shows the award.", "https://i.redd.it/award_images/t5_22cerq/klvxk1wggfd41_Helpful.png", 150));
        awards.add(new Award("award_88fdcafc-57a0-48db-99cc-76276bfaf28b", "GLOBAL", "Press F", "To pay respects.", "https://i.redd.it/award_images/t5_22cerq/tcofsbf92md41_PressF.png", 150));
        awards.add(new Award("award_a7f9cbd7-c0f1-4569-a913-ebf8d18de00b", "GLOBAL", "Take My Money", "I'm buying what you're selling", "https://i.redd.it/award_images/t5_22cerq/9jr8pv84v7i51_TakeMyMoney.png", 150));
        awards.add(new Award("award_a9968927-3f72-4af9-8bcd-aaf804838dc6", "GLOBAL", "Back Away", "...slowly", "https://i.redd.it/award_images/t5_22cerq/t5z1oc8t2ug51_BackAway.png", 200));
        awards.add(new Award("award_e813313c-1002-49bf-ac37-e966710f605f", "GLOBAL", "Giggle", "Innocent laughter", "https://www.redditstatic.com/gold/awards/icon/Giggle_512.png", 200));
        awards.add(new Award("award_1703f934-cf44-40cc-a96d-3729d0b48262", "GLOBAL", "I'd Like to Thank...", "My kindergarten teacher, my cat, my mom, and you.", "https://i.redd.it/award_images/t5_22cerq/8ad2jffnclf41_Thanks.png", 200));
        awards.add(new Award("award_b28d9565-4137-433d-bb65-5d4aa82ade4c", "GLOBAL", "I'm Deceased", "Call an ambulance, I'm laughing too hard.", "https://i.redd.it/award_images/t5_22cerq/2jd92wtn25g41_ImDeceased.png", 200));
        awards.add(new Award("award_4922c1be-3646-4d62-96ea-19a56798df51", "GLOBAL", "Looking", "I can't help but look.", "https://i.redd.it/award_images/t5_22cerq/kjpl76213ug51_Looking.png", 200));
        awards.add(new Award("award_9ee30a8f-463e-4ef7-9da9-a09f270ec026", "GLOBAL", "Stonks Falling", "Losing value fast.", "https://i.redd.it/award_images/t5_22cerq/ree13odobef41_StonksFalling.png", 200));
        awards.add(new Award("award_d125d124-5c03-490d-af3d-d07c462003da", "GLOBAL", "Stonks Rising", "To the MOON.", "https://i.redd.it/award_images/t5_22cerq/s5edqq9abef41_StonksRising.png", 200));
        awards.add(new Award("award_dc391ef9-0df8-468f-bd3c-7b177092de35", "GLOBAL", "This is 2020", "Every reason to be alarmed", "https://i.redd.it/award_images/t5_22cerq/ncon692ev7i51_Thisis2020.png", 200));
        awards.add(new Award("award_11be92ba-509e-46d3-991b-593239006521", "GLOBAL", "1UP", "Extra life", "https://www.redditstatic.com/gold/awards/icon/Levelup_512.png", 250));
        awards.add(new Award("award_351f8639-ee3b-4def-adf3-6b39980c278a", "GLOBAL", "2020 Vision", "Looking into the present.", "https://i.redd.it/award_images/t5_22cerq/48eychq6e9741_2020Vision.png", 300));
        awards.add(new Award("award_cc299d65-77de-4828-89de-708b088349a0", "GLOBAL", "GOAT", "Historical anomaly - greatest in eternity.", "https://i.redd.it/award_images/t5_22cerq/x52x5be57fd41_GOAT.png", 300));
        awards.add(new Award("award_8dc476c7-1478-4d41-b940-f139e58f7756", "GLOBAL", "Got the W", "", "https://i.redd.it/award_images/t5_22cerq/9avdcwgupta41_GottheW.png", 300));
        awards.add(new Award("award_28e8196b-d4e9-45bc-b612-cd4c7d3ed4b3", "GLOBAL", "Rocket Like", "When an upvote just isn't enough, smash the Rocket Like.", "https://i.redd.it/award_images/t5_22cerq/94pn64yuas941_RocketLike.png", 300));
        awards.add(new Award("award_3e000ecb-c1a4-49dc-af14-c8ac2029ca97", "GLOBAL", "Table Flip", "ARGH!", "https://i.redd.it/award_images/t5_22cerq/a05z7bb9v7i51_TableFlip.png", 300));
        awards.add(new Award("award_725b427d-320b-4d02-8fb0-8bb7aa7b78aa", "GLOBAL", "Updoot", "Sometimes you just got to doot.", "https://i.redd.it/award_images/t5_22cerq/7atjjqpy1mc41_Updoot.png", 300));
        awards.add(new Award("award_c043e7ef-8514-4862-99c6-45e36cec4f66", "GLOBAL", "OOF", "That hurts", "https://www.redditstatic.com/gold/awards/icon/OOF_512.png", 325));
        awards.add(new Award("award_2bc47247-b107-44a8-a78c-613da21869ff", "GLOBAL", "To The Stars", "Boldly go where we haven't been in a long, long time.", "https://www.redditstatic.com/gold/awards/icon/Rocket_512.png", 325));
        awards.add(new Award("award_2ff1fdd0-ff73-47e6-a43c-bde6d4de8fbd", "GLOBAL", "Into the Magic Portal", "Hope to make it to the other side.", "https://www.redditstatic.com/gold/awards/icon/TeleportIn_512.png", 350));
        awards.add(new Award("award_7fe72f36-1141-4a39-ba76-0d481889b390", "GLOBAL", "Out of the Magic Portal", "That was fun, but I'm glad to be back", "https://www.redditstatic.com/gold/awards/icon/TeleportOut_512.png", 350));
        awards.add(new Award("award_f7a4fd5e-7cd1-4c11-a1c9-c18d05902e81", "GLOBAL", "Crab Rave", "[Happy crab noises]", "https://www.redditstatic.com/gold/awards/icon/CrabRave_512.png", 400));
        awards.add(new Award("award_43c43a35-15c5-4f73-91ef-fe538426435a", "GLOBAL", "Bless Up (Pro)", "Prayers up for the blessed. Gives 100 Coins to both the author and the community.", "https://i.redd.it/award_images/t5_22cerq/xe5mw55w5v541_BlessUp.png", 500));
        awards.add(new Award("award_27d3176c-b388-4616-80ec-11b8ece5b7ee", "GLOBAL", "Snoo Nice", "Gives the author a week of Reddit Premium and 100 Coins to do with as they please.", "https://i.redd.it/award_images/t5_22cerq/9r7hexe6pta41_SnooNice.png", 500));
        awards.add(new Award("award_35c78e6e-507b-4f1d-b3d8-ed43840909a8", "GLOBAL", "Pot o' Coins", "The treasure at the end of the rainbow. Gives the author 800 Coins to do with as they please.", "https://i.redd.it/award_images/t5_22cerq/wg3lzllyg9n41_PotoCoins.png", 1000));
        awards.add(new Award("award_5eac457f-ebac-449b-93a7-eb17b557f03c", "PREMIUM", "LOVE!", "When you follow your heart, love is the answer", "https://i.redd.it/award_images/t5_22cerq/j3azv69qjfn51_LOVE.png", 20));
        awards.add(new Award("award_abb865cf-620b-4219-8777-3658cf9091fb", "PREMIUM", "Starstruck", "Can't stop seeing stars", "https://www.redditstatic.com/gold/awards/icon/Starstruck_512.png", 20));
        awards.add(new Award("award_b4ff447e-05a5-42dc-9002-63568807cfe6", "PREMIUM", "All-Seeing Upvote", "A glowing commendation for all to see", "https://i.redd.it/award_images/t5_22cerq/rg960rc47jj41_All-SeeingUpvote.png", 30));
        awards.add(new Award("award_a2506925-fc82-4d6c-ae3b-b7217e09d7f0", "PREMIUM", "Narwhal Salute", "A golden splash of respect", "https://i.redd.it/award_images/t5_22cerq/80j20o397jj41_NarwhalSalute.png", 30));
        awards.add(new Award("award_c4b2e438-16bb-4568-88e7-7893b7662944", "PREMIUM", "Wholesome Seal of Approval", "A glittering stamp for a feel-good thing", "https://i.redd.it/award_images/t5_22cerq/b9ks3a5k7jj41_WholesomeSealofApproval.png", 30));
        awards.add(new Award("award_9663243a-e77f-44cf-abc6-850ead2cd18d", "PREMIUM", "Bravo Grande!", "For an especially amazing showing.", "https://www.redditstatic.com/gold/awards/icon/SnooClappingPremium_512.png", 75));
        awards.add(new Award("award_92cb6518-a71a-4217-9f8f-7ecbd7ab12ba", "PREMIUM", "Take My Power", "Add my power to yours.", "https://www.redditstatic.com/gold/awards/icon/TakeMyPower_512.png", 75));
        awards.add(new Award("award_5fb42699-4911-42a2-884c-6fc8bdc36059", "APPRECIATION", "Cake", "Did someone say... cake?", "https://i.redd.it/award_images/t5_22cerq/aoa99054n2s41_Cake.png", 150));
        awards.add(new Award("award_f7562045-905d-413e-9ed2-0a16d4bfe349", "APPRECIATION", "Plus One", "You officially endorse and add your voice to the crowd.", "https://i.redd.it/award_images/t5_22cerq/6vgr8y21i9741_PlusOne.png", 200));
        awards.add(new Award("award_2adc49e8-d6c9-4923-9293-2bfab1648569", "APPRECIATION", "Awesome Answer", "For a winning take and the kind soul who nails a question. Gives 100 Coins to both the author and the community.", "https://i.redd.it/award_images/t5_22cerq/71v56o5a5v541_AwesomeAnswer.png", 250));
        awards.add(new Award("award_cc540de7-dfdb-4a68-9acf-6f9ce6b17d21", "APPRECIATION", "It's Cute!", "You made me UwU.", "https://i.redd.it/award_images/t5_22cerq/n94bgm83in941_ItsCute.png", 250));
        awards.add(new Award("award_9583d210-a7d0-4f3c-b0c7-369ad579d3d4", "APPRECIATION", "Mind Blown", "When a thing immediately combusts your brain. Gives 100 Coins to both the author and the community.", "https://i.redd.it/award_images/t5_22cerq/wa987k0p4v541_MindBlown.png", 250));
        awards.add(new Award("award_d306c865-0d49-4a36-a1ab-a4122a0e3480", "APPRECIATION", "Original", "When something new and creative wows you. Gives 100 Coins to both the author and the community.", "https://i.redd.it/award_images/t5_22cerq/b8xt4z8yajz31_Original.png", 250));
        awards.add(new Award("award_a67d649d-5aa5-407e-a98b-32fd9e3a9696", "APPRECIATION", "Today I Learned", "The more you know... Gives 100 Coins to both the author and the community.", "https://i.redd.it/award_images/t5_22cerq/bph2png4ajz31_TodayILearned.png", 250));
        awards.add(new Award("award_d48aad4b-286f-4a3a-bb41-ec05b3cd87cc", "APPRECIATION", "Yas Queen", "YAAAAAAAAAAASSS.", "https://i.redd.it/award_images/t5_22cerq/kthj3e4h3bm41_YasQueen.png", 250));
        awards.add(new Award("award_a9009ea5-1a36-42ae-aab2-5967563ee054", "APPRECIATION", "Heart Eyes", "For love at first sight. Gives 100 Coins to both the author and the community.", "https://i.redd.it/award_images/t5_22cerq/12kz7a7j4v541_HeartEyes.png", 500));
        awards.add(new Award("award_a7a04d6a-8dd8-41bb-b906-04fa8f144014", "APPRECIATION", "Made Me Smile", "When you're smiling before you know it. Gives 100 Coins to both the author and the community.", "https://i.redd.it/award_images/t5_22cerq/hwnbr9l67s941_MadeMeSmile.png", 500));
        return awards;
    }

    public String getId() {
        return id;
    }

    public String getAwardSubType() {
        return awardSubType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public int getCoinPrice() {
        return coinPrice;
    }
}
