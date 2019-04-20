/*
 * This file is part of the L2J Mobius project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2j.gameserver.taskmanager.tasks;

import org.l2j.commons.database.DatabaseFactory;
import org.l2j.gameserver.Config;
import org.l2j.gameserver.data.sql.impl.CharNameTable;
import org.l2j.gameserver.enums.MailType;
import org.l2j.gameserver.instancemanager.MailManager;
import org.l2j.gameserver.model.entity.Message;
import org.l2j.gameserver.model.itemcontainer.Mail;
import org.l2j.gameserver.taskmanager.Task;
import org.l2j.gameserver.taskmanager.TaskManager;
import org.l2j.gameserver.taskmanager.TaskManager.ExecutableTask;
import org.l2j.gameserver.taskmanager.TaskType;
import org.l2j.gameserver.util.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author Nyaran
 */
public class TaskBirthday extends Task {
    private static final String NAME = "birthday";
    private static final String QUERY = "SELECT charId, createDate FROM characters WHERE createDate LIKE ?";
    private static final Calendar _today = Calendar.getInstance();
    private int _count = 0;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void onTimeElapsed(ExecutableTask task) {
        final Calendar lastExecDate = Calendar.getInstance();
        final long lastActivation = task.getLastActivation();

        if (lastActivation > 0) {
            lastExecDate.setTimeInMillis(lastActivation);
        }

        final String rangeDate = "[" + Util.getDateString(lastExecDate.getTime()) + "] - [" + Util.getDateString(_today.getTime()) + "]";

        for (; !_today.before(lastExecDate); lastExecDate.add(Calendar.DATE, 1)) {
            checkBirthday(lastExecDate.get(Calendar.YEAR), lastExecDate.get(Calendar.MONTH), lastExecDate.get(Calendar.DATE));
        }

        LOGGER.info("BirthdayManager: " + _count + " gifts sent. " + rangeDate);
    }

    private void checkBirthday(int year, int month, int day) {
        try (Connection con = DatabaseFactory.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement(QUERY)) {
            statement.setString(1, "%-" + getNum(month + 1) + "-" + getNum(day));
            try (ResultSet rset = statement.executeQuery()) {
                while (rset.next()) {
                    final int playerId = rset.getInt("charId");
                    final Calendar createDate = Calendar.getInstance();
                    createDate.setTime(rset.getDate("createDate"));

                    final int age = year - createDate.get(Calendar.YEAR);
                    if (age <= 0) {
                        continue;
                    }

                    String text = Config.ALT_BIRTHDAY_MAIL_TEXT;

                    if (text.contains("$c1")) {
                        text = text.replace("$c1", CharNameTable.getInstance().getNameById(playerId));
                    }
                    if (text.contains("$s1")) {
                        text = text.replace("$s1", String.valueOf(age));
                    }

                    final Message msg = new Message(playerId, Config.ALT_BIRTHDAY_MAIL_SUBJECT, text, MailType.BIRTHDAY);

                    final Mail attachments = msg.createAttachments();
                    attachments.addItem("Birthday", Config.ALT_BIRTHDAY_GIFT, 1, null, null);

                    MailManager.getInstance().sendMessage(msg);
                    _count++;
                }
            }
        } catch (SQLException e) {
            LOGGER.warn("Error checking birthdays. ", e);
        }

        // If character birthday is 29-Feb and year isn't leap, send gift on 28-feb
        final GregorianCalendar calendar = new GregorianCalendar();
        if ((month == Calendar.FEBRUARY) && (day == 28) && !calendar.isLeapYear(_today.get(Calendar.YEAR))) {
            checkBirthday(year, Calendar.FEBRUARY, 29);
        }
    }

    /**
     * @param num the number to format.
     * @return the formatted number starting with a 0 if it is lower or equal than 10.
     */
    private String getNum(int num) {
        return (num <= 9) ? "0" + num : String.valueOf(num);
    }

    @Override
    public void initializate() {
        TaskManager.addUniqueTask(NAME, TaskType.GLOBAL_TASK, "1", "06:30:00", "");
    }
}
