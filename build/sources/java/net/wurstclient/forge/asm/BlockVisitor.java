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

public final class BlockVisitor extends WurstClassVisitor
{
	public BlockVisitor(ClassVisitor cv, boolean obf)
	{
		super(cv);

		String shouldSideDesc =
			"(Lnet/minecraft/world/IBlockAccess;IIII)Z";
		String canRenderInPassDesc = "(I)Z";

		registerMethodVisitor("shouldSideBeRendered", shouldSideDesc,
			mv -> new ShouldSideBeRenderedVisitor(mv));
		registerMethodVisitor("func_149646_a", shouldSideDesc,
			mv -> new ShouldSideBeRenderedVisitor(mv));
		registerMethodVisitor("a", shouldSideDesc,
			mv -> new ShouldSideBeRenderedVisitor(mv));

		registerMethodVisitor("getAmbientOcclusionLightValue", "()F",
			mv -> new GetAmbientOcclusionLightValueVisitor(mv));
		registerMethodVisitor("func_149685_I", "()F",
			mv -> new GetAmbientOcclusionLightValueVisitor(mv));
		registerMethodVisitor("I", "()F",
			mv -> new GetAmbientOcclusionLightValueVisitor(mv));

		registerMethodVisitor("isNormalCube", "()Z",
			mv -> new IsNormalCubeVisitor(mv));
		registerMethodVisitor("func_149721_r", "()Z",
			mv -> new IsNormalCubeVisitor(mv));
		registerMethodVisitor("r", "()Z", mv -> new IsNormalCubeVisitor(mv));

		registerMethodVisitor("isOpaqueCube", "()Z",
			mv -> new IsOpaqueCubeVisitor(mv));
		registerMethodVisitor("func_149662_c", "()Z",
			mv -> new IsOpaqueCubeVisitor(mv));
		registerMethodVisitor("c", "()Z", mv -> new IsOpaqueCubeVisitor(mv));

		registerMethodVisitor("canRenderInPass", canRenderInPassDesc,
			mv -> new CanRenderInPassVisitor(mv));
	}

	private static final class ShouldSideBeRenderedVisitor extends MethodVisitor
	{
		private ShouldSideBeRenderedVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}

		@Override
		public void visitInsn(int opcode)
		{
			if(opcode == Opcodes.IRETURN)
			{
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC,
					"net/wurstclient/forge/compatibility/WEventFactory",
					"shouldSideBeRendered",
					"(ZLnet/minecraft/block/Block;)Z", false);
			}

			super.visitInsn(opcode);
		}
	}

	private static final class GetAmbientOcclusionLightValueVisitor
		extends MethodVisitor
	{
		private GetAmbientOcclusionLightValueVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}

		@Override
		public void visitInsn(int opcode)
		{
			if(opcode == Opcodes.FRETURN)
			{
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC,
					"net/wurstclient/forge/compatibility/WEventFactory",
					"getAmbientOcclusionLightValue",
					"(FLnet/minecraft/block/Block;)F", false);
			}

			super.visitInsn(opcode);
		}
	}

	private static final class IsNormalCubeVisitor extends MethodVisitor
	{
		private IsNormalCubeVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}

		@Override
		public void visitInsn(int opcode)
		{
			if(opcode == Opcodes.IRETURN)
			{
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC,
					"net/wurstclient/forge/compatibility/WEventFactory",
					"isNormalCube", "(ZLnet/minecraft/block/Block;)Z", false);
			}

			super.visitInsn(opcode);
		}
	}

	private static final class IsOpaqueCubeVisitor extends MethodVisitor
	{
		private IsOpaqueCubeVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}

		@Override
		public void visitInsn(int opcode)
		{
			if(opcode == Opcodes.IRETURN)
			{
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC,
					"net/wurstclient/forge/compatibility/WEventFactory",
					"isOpaqueCube", "(ZLnet/minecraft/block/Block;)Z", false);
			}

			super.visitInsn(opcode);
		}
	}

	private static final class CanRenderInPassVisitor extends MethodVisitor
	{
		private CanRenderInPassVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}

		@Override
		public void visitInsn(int opcode)
		{
			if(opcode == Opcodes.IRETURN)
			{
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitVarInsn(Opcodes.ILOAD, 1);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC,
					"net/wurstclient/forge/compatibility/WEventFactory",
					"canRenderInPass",
					"(ZLnet/minecraft/block/Block;I)Z", false);
			}

			super.visitInsn(opcode);
		}
	}
}
