/* QNotified - An Xposed module for QQ/TIM
 * Copyright (C) 2019-2021 xenonhydride@gmail.com
 * https://github.com/ferredoxin/QNotified
 *
 * This software is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see
 * <https://www.gnu.org/licenses/>.
 */
package ltd.nextalone.hook

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.ViewCompat
import ltd.nextalone.util.*
import me.kyuubiran.util.getExFriendCfg
import me.singleneuron.qn_kernel.data.requireMinQQVersion
import me.singleneuron.util.QQVersion
import nil.nadph.qnotified.hook.CommonDelayableHook
import nil.nadph.qnotified.ui.CustomDialog
import nil.nadph.qnotified.ui.HighContrastBorder
import nil.nadph.qnotified.ui.ViewBuilder
import nil.nadph.qnotified.util.Toasts
import nil.nadph.qnotified.util.Utils
import java.util.*


object ChatWordsCount : CommonDelayableHook("na_chat_words_count_kt") {
    private const val msgCfg = "na_chat_words_count_kt_msg"
    private const val wordsCfg = "na_chat_words_count_kt_words"
    private const val emoCfg = "na_chat_words_count_kt_emo"
    private const val timeCfg = "na_chat_words_count_kt_time"
    private const val colorCfg = "na_chat_words_count_kt_color"
    private const val strCfg = "na_chat_words_count_kt_str"
    override fun initOnce(): Boolean {
        return try {
            "com.tencent.mobileqq.activity.QQSettingMe".clazz.hookBeforeAllConstructors {
                "Lcom/tencent/mobileqq/activity/QQSettingMe;->a()V".method.hookAfter(this) {
                    val isToday = Date().today == getExFriendCfg().getStringOrDefault(timeCfg, "")
                    val relativeLayout = (it.thisObject.get("a", ViewGroup::class.java) as ViewGroup).findQQView<RelativeLayout>("ivc")
                    val textView = (relativeLayout?.parent as FrameLayout).findViewById<TextView>(nil.nadph.qnotified.R.id.chat_words_count)
                    var str = getExFriendCfg().getStringOrDefault(strCfg, "今日已发送 %1 条消息，共 %2 字，表情包 %3 个")
                    val msg = if (isToday) getExFriendCfg().getIntOrDefault(msgCfg, 0) else 0
                    val words = if (isToday) getExFriendCfg().getIntOrDefault(wordsCfg, 0) else 0
                    val emo = if (isToday) getExFriendCfg().getIntOrDefault(emoCfg, 0) else 0
                    str = str.replace("%1", msg.toString()).replace("%2", words.toString()).replace("%3", emo.toString())
                    textView.text = str
                }
            }
            "com.tencent.mobileqq.activity.QQSettingMe".clazz.hookAfterAllConstructors {
                val isToday = Date().today == getExFriendCfg().getStringOrDefault(timeCfg, "")
                val activity: Activity = it.args[0] as Activity
                val relativeLayout = (it.thisObject.get("a", ViewGroup::class.java) as ViewGroup).findQQView<RelativeLayout>("ivc")
                relativeLayout!!.visibility = View.GONE
                val textView = TextView(activity)
                var str = getExFriendCfg().getStringOrDefault(strCfg, "今日已发送 %1 条消息，共 %2 字，表情包 %3 个")
                val msg = if (isToday) getExFriendCfg().getIntOrDefault(msgCfg, 0) else 0
                val words = if (isToday) getExFriendCfg().getIntOrDefault(wordsCfg, 0) else 0
                val emo = if (isToday) getExFriendCfg().getIntOrDefault(emoCfg, 0) else 0
                str = str.replace("%1", msg.toString()).replace("%2", words.toString()).replace("%3", emo.toString())
                textView.text = str
                textView.setTextColor(Color.parseColor(getExFriendCfg().getStringOrDefault(colorCfg, "#FF000000")))
                textView.id = nil.nadph.qnotified.R.id.chat_words_count
                textView.textSize = 15.0f
                textView.setOnClickListener {
                    val dialog = CustomDialog.createFailsafe(activity)
                    val ctx = dialog.context
                    val editText = EditText(ctx)
                    editText.setText(getExFriendCfg().getStringOrDefault(colorCfg, "#ff000000"))
                    editText.textSize = 16f
                    val _5 = Utils.dip2px(activity, 5f)
                    editText.setPadding(_5, _5, _5, _5)
                    ViewCompat.setBackground(editText, HighContrastBorder())
                    val linearLayout = LinearLayout(ctx)
                    linearLayout.addView(editText, ViewBuilder.newLinearLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, _5 * 2))
                    val alertDialog = dialog
                        .setTitle("输入聊天字数统计颜色")
                        .setView(linearLayout)
                        .setPositiveButton("确认") { _, _ ->
                            val color = editText.text.toString()
                            try {
                                Color.parseColor(color)
                                putExFriend(colorCfg, color)
                                dialog.dismiss()
                                Toasts.showToast(activity, Utils.TOAST_TYPE_INFO, "重启以应用设置", Toast.LENGTH_SHORT)
                            } catch (e: IllegalArgumentException) {
                                Toasts.showToast(activity, Utils.TOAST_TYPE_ERROR, "颜色格式不正确", Toast.LENGTH_SHORT)
                            }
                        }
                        .setNegativeButton("取消", null)
                        .setNeutralButton("使用默认值") { _, _ ->
                            putExFriend(colorCfg, "#FF000000")
                            Toasts.showToast(activity, Utils.TOAST_TYPE_INFO, "重启以应用设置", Toast.LENGTH_SHORT)
                        }
                        .create() as AlertDialog
                    alertDialog.show()
                }
                (relativeLayout.parent as FrameLayout).addView(textView)
            }
            "Lcom/tencent/mobileqq/activity/ChatActivityFacade;->a(Lcom/tencent/mobileqq/app/QQAppInterface;Landroid/content/Context;Lcom/tencent/mobileqq/activity/aio/SessionInfo;Ljava/lang/String;Ljava/util/ArrayList;Lcom/tencent/mobileqq/activity/ChatActivityFacade\$SendMsgParams;)[J".method.hookAfter(this) {
                val isToday = Date().today == getExFriendCfg().getStringOrDefault(timeCfg, "")
                if (isToday) {
                    putExFriend(msgCfg, getExFriendCfg().getIntOrDefault(msgCfg, 1) + 1)
                    putExFriend(wordsCfg, getExFriendCfg().getIntOrDefault(wordsCfg, 1) + (it.args[3] as String).length)
                } else {
                    putExFriend(timeCfg, Date().today)
                    putExFriend(msgCfg, 0)
                    putExFriend(wordsCfg, 0)
                }
            }
            "Lcom/tencent/mobileqq/activity/ChatActivityFacade;->a(Lcom/tencent/mobileqq/app/QQAppInterface;Landroid/content/Context;Lcom/tencent/mobileqq/activity/aio/SessionInfo;Ljava/lang/String;ZZZLjava/lang/String;Lcom/tencent/mobileqq/emoticon/EmojiStickerManager\$StickerInfo;Ljava/lang/String;Landroid/os/Bundle;)V".method.hookAfter(this) {
                val isToday = Date().today == getExFriendCfg().getStringOrDefault(timeCfg, "")
                if (isToday) {
                    putExFriend(emoCfg, getExFriendCfg().getIntOrDefault(emoCfg, 0) + 1)
                } else {
                    putExFriend(timeCfg, Date().today)
                    putExFriend(emoCfg, 0)
                }
            }
            true
        } catch (t: Throwable) {
            Utils.log(t)
            false
        }
    }

    override fun isValid(): Boolean = requireMinQQVersion(QQVersion.QQ_8_5_5)

    fun showChatWordsCountDialog(activity: Context) {
        val dialog = CustomDialog.createFailsafe(activity)
        val ctx = dialog.context
        val editText = EditText(ctx)
        editText.textSize = 16f
        val _5 = Utils.dip2px(activity, 5f)
        editText.setPadding(_5, _5, _5, _5)
        editText.setText(getExFriendCfg().getStringOrDefault(strCfg, "今日已发送 %1 条消息，共 %2 字，表情包 %3 个"))
        ViewCompat.setBackground(editText, HighContrastBorder())
        val checkBox = CheckBox(ctx)
        checkBox.text = "开启聊天字数统计"
        checkBox.isChecked = isEnabled
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            isEnabled = isChecked
            when (isChecked) {
                true -> Toasts.showToast(ctx, Toasts.TYPE_INFO, "已开启聊天字数统计", Toasts.LENGTH_SHORT)
                false -> Toasts.showToast(ctx, Toasts.TYPE_INFO, "已关闭聊天字数统计", Toasts.LENGTH_SHORT)
            }
        }
        val linearLayout = LinearLayout(ctx)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.addView(ViewBuilder.subtitle(activity, "%1表示发送消息总数，%2表示发送字数，%3表示发送表情包个数"))
        linearLayout.addView(checkBox, ViewBuilder.newLinearLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, _5 * 2))
        linearLayout.addView(editText, ViewBuilder.newLinearLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, _5 * 2))
        val alertDialog = dialog.setTitle("输入聊天字数统计样式")
            .setView(linearLayout)
            .setCancelable(true)
            .setPositiveButton("确认") { _, _ ->
                val text = editText.text.toString()
                if (text == "") {
                    Toasts.showToast(activity, Utils.TOAST_TYPE_ERROR, "请输入聊天字数统计样式", Toast.LENGTH_SHORT)
                } else {
                    putExFriend(strCfg, text)
                    Toasts.showToast(activity, Utils.TOAST_TYPE_INFO, "设置已保存", Toast.LENGTH_SHORT)
                    dialog.dismiss()
                }
            }.setNeutralButton("使用默认值") { _, _ ->
                putExFriend(strCfg, "今日已发送 %1 条消息，共 %2 字，表情包 %3 个")
            }
            .setNegativeButton("取消", null)
            .create() as AlertDialog
        alertDialog.show()
    }
}
