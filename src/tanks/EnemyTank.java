package tanks;

import basicgraphics.Clock;
import basicgraphics.SpriteCollisionEvent;
import basicgraphics.SpriteComponent;
import basicgraphics.Task;

import java.awt.*;

public abstract class EnemyTank extends Tank {

    private final Color color;

    private long prevTime;
    private long stopWatch;

    public EnemyTank(SpriteComponent sc, Color color) {
        super(sc);
        this.color = color;
        this.prevTime = System.currentTimeMillis();

        Clock.addTask(new Task() {
            @Override
            public void run() {
                if (!isActive()) {
                    return;
                }
                brain(stopWatch);
                var time = System.currentTimeMillis();
                stopWatch += time - prevTime;
                prevTime = time;
                draw();
            }
        });
    }

    public abstract void brain(long stopWatch);

    final void resetStopwatch() {
        stopWatch = 0;
    }

    @Override
    public void processEvent(SpriteCollisionEvent ev) {
        if (ev.xlo) {
            setVelX(1.5);
        } else if (ev.xhi) {
            setVelX(-1.5);
        }

        if (ev.ylo) {
            setVelY(1.5);
        } else if (ev.yhi) {
            setVelY(-1.5);
        }
    }

    @Override
    final public Color getColor() {
        return color;
    }
}
