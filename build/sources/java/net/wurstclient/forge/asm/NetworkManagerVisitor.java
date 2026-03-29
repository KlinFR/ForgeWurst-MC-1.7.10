/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GNU General Public License was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class NetworkManagerVisitor extends WurstClassVisitor
{
	public NetworkManagerVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);

		String packet = "net/minecraft/network/Packet";
		String listenerArray = "[Lio/netty/util/concurrent/GenericFutureListener;";

		String scheduleOutboundPacketDesc = "(L" + packet + ";" + listenerArray
			+ ")V";
		registerMethodVisitor("scheduleOutboundPacket",
			scheduleOutboundPacketDesc,
			mv -> new OnSendPacketVisitor(mv));
		registerMethodVisitor("func_150725_a", scheduleOutboundPacketDesc,
			mv -> new OnSendPacketVisitor(mv));

		String channelRead0Desc =
			"(Lio/netty/channel/ChannelHandlerContext;L" + packet + ";)V";
		registerMethodVisitor("channelRead0", channelRead0Desc,
			mv -> new OnReceivePacketVisitor(mv));
	}

	private static final class OnSendPacketVisitor extends MethodVisitor
	{
		private OnSendPacketVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}

		@Override
		public void visitCode()
		{
			super.visitCode();

			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"onSendPacket", "(Lnet/minecraft/network/Packet;)Lnet/minecraft/network/Packet;",
				false);
			mv.visitVarInsn(Opcodes.ASTORE, 1);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			Label continueLabel = new Label();
			mv.visitJumpInsn(Opcodes.IFNONNULL, continueLabel);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitLabel(continueLabel);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		}
	}

	private static final class OnReceivePacketVisitor extends MethodVisitor
	{
		private OnReceivePacketVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}

		@Override
		public void visitCode()
		{
			super.visitCode();

			mv.visitVarInsn(Opcodes.ALOAD, 2);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"onReceivePacket", "(Lnet/minecraft/network/Packet;)Z",
				false);
			Label continueLabel = new Label();
			mv.visitJumpInsn(Opcodes.IFNE, continueLabel);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitLabel(continueLabel);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		}
	}
}
