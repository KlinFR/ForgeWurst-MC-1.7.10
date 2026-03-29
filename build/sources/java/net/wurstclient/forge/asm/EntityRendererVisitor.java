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

public final class EntityRendererVisitor extends WurstClassVisitor
{
	public EntityRendererVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);

		registerMethodVisitor("hurtCameraEffect", "(F)V",
			mv -> new HurtCameraEffectVisitor(mv));
		registerMethodVisitor("func_78482_e", "(F)V",
			mv -> new HurtCameraEffectVisitor(mv));
		registerMethodVisitor("d", "(F)V",
			mv -> new HurtCameraEffectVisitor(mv));
		registerMethodVisitor("setupCameraTransform", "(FI)V",
			mv -> new SetupCameraTransformVisitor(mv));
		registerMethodVisitor("func_78479_a", "(FI)V",
			mv -> new SetupCameraTransformVisitor(mv));
		registerMethodVisitor("a", "(FI)V",
			mv -> new SetupCameraTransformVisitor(mv));
	}

	private static final class HurtCameraEffectVisitor extends MethodVisitor
	{
		private HurtCameraEffectVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}

		@Override
		public void visitCode()
		{
			super.visitCode();
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"hurtCameraEffect", "()Z", false);
			Label continueLabel = new Label();
			mv.visitJumpInsn(Opcodes.IFNE, continueLabel);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitLabel(continueLabel);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		}
	}

	private static final class SetupCameraTransformVisitor extends MethodVisitor
	{
		private boolean foundViewBobbing;

		private SetupCameraTransformVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name,
			String desc)
		{
			super.visitFieldInsn(opcode, owner, name, desc);

			if("viewBobbing".equals(name) || "field_74336_f".equals(name)
				|| "f".equals(name))
				foundViewBobbing = true;
		}

		@Override
		public void visitJumpInsn(int opcode, Label label)
		{
			super.visitJumpInsn(opcode, label);

			if(!foundViewBobbing)
				return;
			foundViewBobbing = false;

			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				"net/wurstclient/forge/compatibility/WEventFactory",
				"cameraTransformViewBobbing", "()Z", false);
			mv.visitJumpInsn(Opcodes.IFEQ, label);
		}
	}
}
