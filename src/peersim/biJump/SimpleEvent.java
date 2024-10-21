package peersim.biJump;

import peersim.core.CommonState;

/**
 * @author <a href="kw_wang_@outlook.com">Kaiwen Wang</a>
 * @version v1.0
 * @since 2024/10/12
 */
public class SimpleEvent {
    protected int type;
    public long timestamp;
    public SimpleEvent() {
        this.timestamp = CommonState.getTime();
    }
    public SimpleEvent(int type) {
        this.type = type;
        this.timestamp = CommonState.getTime();
    }
    public int getType() {
        return type;
    }
}
