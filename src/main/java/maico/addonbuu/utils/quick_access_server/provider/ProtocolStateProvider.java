package maico.addonbuu.utils.quick_access_server.provider;
public class ProtocolStateProvider {
    public enum State { CONNECTING, ESTABLISHED, CLOSED }
    public State getCurrentState() { return State.ESTABLISHED; }
}
