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

package l1j.jrwz.server;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.jrwz.server.command.L1Commands;
import l1j.jrwz.server.command.executor.L1CommandExecutor;
import l1j.jrwz.server.model.Instance.L1PcInstance;
import l1j.jrwz.server.serverpackets.S_ServerMessage;
import l1j.jrwz.server.serverpackets.S_SystemMessage;
import l1j.jrwz.server.templates.L1Command;

// Referenced classes of package l1j.jrwz.server:
// ClientThread, Shutdown, IpTable, MobTable,
// PolyTable, IdFactory
//

public class GMCommands {
    private static Logger _log = Logger.getLogger(GMCommands.class.getName());

    private static GMCommands _instance;

    private static Map<Integer, String> _lastCommands = new HashMap<Integer, String>();

    public static GMCommands getInstance() {
        if (_instance == null) {
            _instance = new GMCommands();
        }
        return _instance;
    }

    private GMCommands() {
    }

    private String complementClassName(String className) {
        // 如果包涵 . 則認為他已經有完整路徑，所以直接丟回去
        if (className.contains(".")) {
            return className;
        }

        // 如果沒有點的話則自動幫他補完前面的路徑
        return "l1j.jrwz.server.command.executor." + className;
    }

    private boolean executeDatabaseCommand(L1PcInstance pc, String name,
            String arg) {
        try {
            L1Command command = L1Commands.get(name);
            if (command == null) {
                return false;
            }
            if (pc.getAccessLevel() < command.getLevel()) {
                pc.sendPackets(new S_ServerMessage(74, "指令" + name)); // \f1%0は使用できません。
                return true;
            }

            Class<?> cls = Class.forName(complementClassName(command
                    .getExecutorClassName()));
            L1CommandExecutor exe = (L1CommandExecutor) cls.getMethod(
                    "getInstance").invoke(null);
            exe.execute(pc, name, arg);
            _log.info(pc.getName() + "使用 ." + name + " " + arg + "的指令。");
            return true;
        } catch (Exception e) {
            _log.log(Level.SEVERE, "error gm command", e);
        }
        return false;
    }

    public void handleCommands(L1PcInstance gm, String cmdLine) {
        StringTokenizer token = new StringTokenizer(cmdLine);
        // 命令，直到第一個空白，並在其後當作參數空格隔開
        String cmd = token.nextToken();
        String param = "";
        while (token.hasMoreTokens()) {
            param = new StringBuilder(param).append(token.nextToken())
                    .append(' ').toString();
        }
        param = param.trim();

        // 將使用過的指令存起來
        if (executeDatabaseCommand(gm, cmd, param)) {
            if (!cmd.equalsIgnoreCase("r")) {
                _lastCommands.put(gm.getId(), cmdLine);
            }
            return;
        }
        if (cmd.equalsIgnoreCase("r")) {
            if (!_lastCommands.containsKey(gm.getId())) {
                gm.sendPackets(new S_ServerMessage(74, "指令" + cmd)); // \f1%0は使用できません。
                return;
            }
            redo(gm, param);
            return;
        }
        gm.sendPackets(new S_SystemMessage("指令 " + cmd + " 不存在。"));
    }

    private void redo(L1PcInstance pc, String arg) {
        try {
            String lastCmd = _lastCommands.get(pc.getId());
            if (arg.isEmpty()) {
                pc.sendPackets(new S_SystemMessage("指令 " + lastCmd + " 重新執行。"));
                handleCommands(pc, lastCmd);
            } else {
                // 引数を変えて実行
                StringTokenizer token = new StringTokenizer(lastCmd);
                String cmd = token.nextToken() + " " + arg;
                pc.sendPackets(new S_SystemMessage("指令 " + cmd + " 執行。"));
                handleCommands(pc, cmd);
            }
        } catch (Exception e) {
            _log.log(Level.SEVERE, e.getLocalizedMessage(), e);
            pc.sendPackets(new S_SystemMessage(".r 指令錯誤。"));
        }
    }
}
