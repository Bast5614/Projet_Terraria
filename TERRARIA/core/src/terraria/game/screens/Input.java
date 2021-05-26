package terraria.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import terraria.game.actors.Inventory.Inventory;
import terraria.game.actors.entities.EntityLoader;
import terraria.game.actors.world.GeneratorMap.MapLoader;

public class Input implements InputProcessor {

    private GameScreen screen;
    private int zoomIndice = 0;

    public Input(GameScreen screen) {
        this.screen = screen;
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case com.badlogic.gdx.Input.Keys.S:
                EntityLoader.saveEntities("test", this.screen.entities);
                MapLoader.saveMap(this.screen.gameMap.getId(), this.screen.gameMap.getName(), this.screen.gameMap.getMap(), this.screen.gameMap.getStartingPoint());
                break;

            case com.badlogic.gdx.Input.Keys.E:
                if (this.screen.inventory.inventoryShow) {
                    this.screen.inventory.inventoryShow = false;
                } else {
                    this.screen.inventory.inventoryShow = true;
                }
                break;

            case com.badlogic.gdx.Input.Keys.ESCAPE:
                if (this.screen.isMenuShow) {
                    this.screen.isMenuShow = false;
                } else {
                    this.screen.isMenuShow = true;
                }
                break;
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        if(Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.CONTROL_LEFT)) {
            float ratio = screen.getStage().getViewport().getWorldWidth()/screen.getStage().getViewport().getWorldHeight();

            if(amountY == 1) {
                //System.out.println(ratio);
                if(this.zoomIndice < 2) {

                    screen.getCamera().zoom += 0.1f;

                    float width = screen.getStage().getViewport().getWorldWidth();

                    this.screen.getStage().getViewport().setScreenWidth(( (screen.getStage().getViewport().getScreenWidth() + (int)(width * 0.1))));
                    this.screen.getStage().getViewport().setScreenHeight( (screen.getStage().getViewport().getScreenHeight() + (int)((width * 0.1)/ratio)));

                    zoomIndice += 1;
                }
            } else if(amountY == -1) {
                if (this.zoomIndice > -3) {
                    screen.getCamera().zoom -= 0.1f;

                    float width = screen.getStage().getViewport().getWorldWidth();

                    this.screen.getStage().getViewport().setScreenWidth( (screen.getStage().getViewport().getScreenWidth() - (int)(width * 0.1)));
                    this.screen.getStage().getViewport().setScreenHeight( (screen.getStage().getViewport().getScreenHeight() - (int)((width * 0.1)/ratio)));

                    zoomIndice -= 1;
                }
            }
        } else {
            if(amountY == 1) {
                if(screen.inventory.getCurrentItems() == 9) {
                    Inventory.setCurrentItems(0);
                } else {
                    Inventory.setCurrentItems(screen.inventory.getCurrentItems() + 1);
                }
            } else if(amountY == -1) {
                if(screen.inventory.getCurrentItems() == 0) {
                    Inventory.setCurrentItems(9);
                } else {
                    Inventory.setCurrentItems(screen.inventory.getCurrentItems() - 1);
                }
            }
        }

        return true;
    }
}
