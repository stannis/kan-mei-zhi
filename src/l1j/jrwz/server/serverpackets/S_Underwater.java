/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */

package l1j.jrwz.server.serverpackets;

import l1j.jrwz.server.Opcodes;

// Referenced classes of package l1j.jrwz.server.serverpackets:
// ServerBasePacket

public class S_Underwater extends ServerBasePacket {

    private static final String _S__19_UNDERWATER = "[S] S_Underwater";

    public S_Underwater(int playerobjecId, int type) {
        writeC(Opcodes.S_OPCODE_UNDERWATER);
        writeD(playerobjecId);
        writeC(type);
    }

    @Override
    public byte[] getContent() {
        return getBytes();
    }

    @Override
    public String getType() {
        return _S__19_UNDERWATER;
    }
}
