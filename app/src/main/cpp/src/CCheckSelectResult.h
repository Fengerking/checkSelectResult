/*******************************************************************************
	File:		CCheckSelectResult.h

	Contains:	the window view header file

	Written by:	Bangfei Jin

	Change History (most recent first):
	2016-12-29		Bangfei			Create file

*******************************************************************************/
#ifndef __CCheckSelectResult_H__
#define __CCheckSelectResult_H__

struct pixelInfo {
	int nX;
	int nY;
	int nValue;
};

struct lineRect {
	int nX;
	int nY;
	int nR;
	int nB;
};

class CCheckSelectResult
{
public:
	CCheckSelectResult(void);
	virtual ~CCheckSelectResult(void);

	virtual int 	OpenFile(const char * pYUVFile);
	virtual int 	CheckBuffer(unsigned char * pYUVBuff);

protected:
	virtual int		cleanPixel(void);
	virtual int		checkLines(pixelInfo ** ppInfo, int nFoundX);
	virtual int		checkResult(void);

	virtual int		releaseBuffs(void);


protected:
	int				m_nWidth;
	int				m_nHeight;
	int				m_nBValue;

	unsigned char * m_pFileBuff;
	pixelInfo **	m_ppBlackPixel;
	int				m_nBlackCount;

	pixelInfo **	m_ppLinePixel;
	int				m_nLinePCount;

public:
	int				m_nFindLines;
	lineRect		m_rcLines[64];
	int				m_nSelResult[64][2];

};
#endif //__CCheckSelectResult_H__