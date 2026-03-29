/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GNU General Public License was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class EntityClientPlayerMPVisitor extends WurstClassVisitor
{
	public EntityClientPlayerMPVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);

		registerMethodVisitor("sendMotionUpdates", "()V",
			mv -> new SendMotionUpdatesVisitor(mv));
		registerMethodVisitor("func_71166_b", "()V",
			mv -> new SendMotionUpdatesVisitor(mv));
	}

	private static final class SendMotionUpdatesVisitor extends MethodVisitor
	{
		private SendMotionUpdatesVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}

		@Override
		public void visitCode()
		{
			super.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"onPreMotion",
				"(Lnet/minecraft/client/entity/EntityPlayerSP;)V", false);
		}

		@Override
		public void visitInsn(int opcode)
		{
			if(opcode == Opcodes.RETURN)
			{
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC,
					"net/wurstclient/forge/compatibility/WEventFactory",
					"onPostMotion",
					"(Lnet/minecraft/client/entity/EntityPlayerSP;)V", false);
			}

			super.visitInsn(opcode);
		}
	}
}
