package org.l2j.gameserver.network.serverpackets;

import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.model.zone.ZoneId;
import org.l2j.gameserver.network.L2GameClient;
import org.l2j.gameserver.network.ServerPacketId;

/**
 * @author Luca Baldi
 */
public class EtcStatusUpdate extends ServerPacket {
    private final L2PcInstance _activeChar;
    private int _mask;

    public EtcStatusUpdate(L2PcInstance activeChar) {
        _activeChar = activeChar;
        _mask = _activeChar.getMessageRefusal() || _activeChar.isChatBanned() || _activeChar.isSilenceMode() ? 1 : 0;
        _mask |= _activeChar.isInsideZone(ZoneId.DANGER_AREA) ? 2 : 0;
        _mask |= _activeChar.hasCharmOfCourage() ? 4 : 0;
    }

    @Override
    public void writeImpl(L2GameClient client) {
        writeId(ServerPacketId.ETC_STATUS_UPDATE);

        writeByte((byte) _activeChar.getCharges()); // 1-7 increase force, lvl
        writeInt(_activeChar.getWeightPenalty()); // 1-4 weight penalty, lvl (1=50%, 2=66.6%, 3=80%, 4=100%)
        writeByte((byte) _activeChar.getExpertiseWeaponPenalty()); // Weapon Grade Penalty [1-4]
        writeByte((byte) _activeChar.getExpertiseArmorPenalty()); // Armor Grade Penalty [1-4]
        writeByte((byte) 0); // Death Penalty [1-15, 0 = disabled)], not used anymore in Ertheia
        writeByte((byte) _activeChar.getChargedSouls());
        writeByte((byte) _mask);
    }

}
