/*
**
**  Matrix
**
**  Matrix is just a wrapper to be able to give an array a name
**
*/

SuperDiffuse_Matrix {
	var m_array, m_name;

	*new { | sizeX, sizeY, name |
		^super.new.init(sizeX, sizeY, name);
	}

	*newFrom { | matrix, name |
		if(matrix.isKindOf(SuperDiffuse_Matrix))
		{
			^super.new.initFrom(matrix, name);
		}
		{
			PrimitiveFailedError(this).throw;
		};
	}

	init { | sizeX, sizeY, name |
		m_array = Array.fill(sizeX, { Array.fill(sizeY,{0}) });
		m_name = name;
	}

	initFrom { | matrix, name |
		m_array = matrix.matrix.deepCopy;
		m_name = name;
	}

	name {
		^m_name;
	}

	name_ { | name |
		if(m_name != name)
		{
			m_name = name;
		}
	}

	matrix {
		^m_array;
	}

	matrix_ { | array |
		m_array.free;
		m_array = array.deepCopy;
	}
}