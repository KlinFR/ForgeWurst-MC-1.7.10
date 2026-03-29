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

public final class PlayerControllerMPVisitor extends WurstClassVisitor
{
	public PlayerControllerMPVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);

		String onPlayerDamageBlockDesc = "(IIII)Z";
		registerMethodVisitor("onPlayerDamageBlock", onPlayerDamageBlockDesc,
			mv -> new OnPlayerDamageBlockVisitor(mv));
		registerMethodVisitor("func_78759_c", onPlayerDamageBlockDesc,
			mv -> new OnPlayerDamageBlockVisitor(mv));
	}

	private static final class OnPlayerDamageBlockVisitor extends MethodVisitor
	{
		private OnPlayerDamageBlockVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}

		@Override
		public void visitCode()
		{
			super.visitCode();

			mv.visitVarInsn(Opcodes.ILOAD, 1);
			mv.visitVarInsn(Opcodes.ILOAD, 2);
			mv.visitVarInsn(Opcodes.ILOAD, 3);
			mv.visitVarInsn(Opcodes.ILOAD, 4);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"onPlayerDamageBlock", "(IIII)V", false);
		}
	}
}
