package terraria.game.actors.world.GeneratorMap;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import terraria.game.actors.world.GeneratorMap.Noise.*;
import terraria.game.actors.world.TileType;
import java.util.Random;


public class MapLoader {

    private static Json json = new Json();
    public static final int SIZE_HEIGHT = 256;
    public static final int SIZE_WIDTH = 1024;

    /**
     * On obtient une mapProcédurale :
     * lut dans un fichier si elle existre
     * En generant une map random qui sera enregistrer dans un nouveau fichier si elle n'existe pas
     *
     * @param id
     * @param name
     * @return
     */
    public static DataMap loadMap(String id, String name) {

        Gdx.files.local("maps/").file().mkdirs();

        FileHandle file = Gdx.files.local("maps/" + id + ".map");
        if (file.exists()) {
            DataMap mapData = json.fromJson(DataMap.class, file.readString());
            return mapData;
        } else {

            DataMap mapData = generateRandomMap(id, name);
            saveMap(mapData.id, mapData.name, mapData.map, mapData.startingPoint);
            return mapData;
        }
    }

    /**
     * On Sauvegarde notre map dans un fichier [id].map
     *
     * @param id
     * @param name
     * @param map
     */
    public static void saveMap(String id, String name, int[][][] map, int[] startingPoint) {
        DataMap mapData = new DataMap();
        mapData.id = id;
        mapData.name = name;
        mapData.map = map;
        mapData.startingPoint = startingPoint;
        mapData.size_height = SIZE_HEIGHT;
        mapData.size_width = SIZE_WIDTH;

        Gdx.files.local("maps/").file().mkdirs();
        FileHandle file = Gdx.files.local("maps/" + id + ".map");
        file.writeString(json.prettyPrint(mapData), false);
    }

    /**
     * On genere une map à trois dimension avec un id et un nom donné en argument
     *
     * @param id
     * @param name
     * @return
     */
    public static DataMap generateRandomMap(String id, String name) {
        DataMap mapData = new DataMap();
        mapData.id = id;
        mapData.name = id;
        mapData.map = new int[3][SIZE_HEIGHT][SIZE_WIDTH];

        Random rand = new Random();
        int random = rand.nextInt(1000);
        System.out.println("random : " + random);

        for (int col = 0; col < SIZE_WIDTH; col++) {


            float LimitOfGrasses = PerlinNoise.PerlinNoise1D((float) (random * 0.1), 0.25f, 1);
            LimitOfGrasses = (float) MettreALEchelleNoise(LimitOfGrasses, 16, 16); //16
            float LimitOfRocks = PerlinNoise.PerlinNoise1D((float) (random * 0.1), 0.60f, 1);
            LimitOfRocks = (float) MettreALEchelleNoise(LimitOfRocks, 40, 10);//14.2

            if (col == SIZE_WIDTH / 2) {
                starting((int)LimitOfGrasses,  (int)LimitOfRocks, random,  col,mapData);
                System.out.println("current + 0 :  "+(int)LimitOfGrasses + "  " + (int)LimitOfRocks + "\n");
            }
            if (col == SIZE_WIDTH / 2 + 1){
                System.out.println("current + 1 :  "+(int)LimitOfGrasses + "  " + (int)LimitOfRocks + "\n");
            }
            if (col == SIZE_WIDTH / 2 + 2){
                System.out.println("current + 2 :  "+(int)LimitOfGrasses + "  " + (int)LimitOfRocks + "\n");
            }
            if (col == SIZE_WIDTH / 2 + 3){
                System.out.println("current + 3 :  "+(int)LimitOfGrasses + "  " + (int)LimitOfRocks + "\n");
            }


            new GeneratorSoil().GenerateSoil((int) LimitOfGrasses, (int) LimitOfRocks, col, mapData, random);
            new GeneratorSubSoil().GenerateSubSoil((int) LimitOfGrasses, (int) LimitOfRocks, col, mapData, random);
            generatorLightMap(mapData, (int) LimitOfGrasses, (int) LimitOfRocks, col);

            random++;
        }

        return mapData;

    }

    protected static void generatorLightMap(DataMap mapData, int LimitOfGrasses, int LimitOfRocks, int col) {

        int row;
        if (LimitOfGrasses < LimitOfRocks) {
            row = LimitOfGrasses;
        } else {
            row = LimitOfRocks;
        }
        mapData.map[2][row][col] = TileType.FILTRE2.getId();
        row++;
        mapData.map[2][row][col] = TileType.FILTRE1.getId();
        row++;
        mapData.map[2][row][col] = TileType.FILTRE0.getId();
        row++;

        while (row < SIZE_HEIGHT) {
            mapData.map[2][row][col] = TileType.lightOff.getId();
            row++;
        }
    }


    public static float MettreALEchelleNoise(float noise, double amplitude, double AxeVertical) {
        noise = noise + 1;
        noise = (float) (noise * (amplitude) / 2);
        noise = (float) (noise + SIZE_HEIGHT / 2 + (AxeVertical));
        return noise;
    }

    public static void starting(int LimitOfGrasses, int LimitOfRocks, int random, int col, DataMap mapData) {

        boolean starting = false;
        int i = 1;

        while (!starting) {


            int nextLimitOfGrasses = (int) MettreALEchelleNoise(PerlinNoise.PerlinNoise1D((float) ((random + i) * 0.1), 0.25f, 1), 16, 16); //16
            int nextLimitOfRocks = (int) MettreALEchelleNoise(PerlinNoise.PerlinNoise1D((float) ((random + i) * 0.1), 0.60f, 1), 40, 10);//14.2

            System.out.println("next : " + i +" "+ (int)nextLimitOfGrasses + "  " + (int)nextLimitOfRocks + "\n");

            if (LimitOfGrasses < LimitOfRocks) {

                    if (nextLimitOfGrasses >= LimitOfGrasses) {
                        mapData.startingPoint = new int[]{(int) (SIZE_HEIGHT - LimitOfGrasses), col + (i-1)};
                        starting = true;
                    }
                    else{
                        LimitOfGrasses = nextLimitOfGrasses;
                        LimitOfRocks = nextLimitOfRocks;
                        i++;
                    }

            } else {
                    if(nextLimitOfRocks >= LimitOfRocks && nextLimitOfGrasses >= LimitOfRocks) {
                        mapData.startingPoint = new int[]{ (SIZE_HEIGHT - LimitOfRocks), col};
                        starting = true;
                    }
                    else{
                        LimitOfGrasses = nextLimitOfGrasses;
                        LimitOfRocks = nextLimitOfRocks;
                        i++;
                    }
            }


        }

    }
}